package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.util.model.LineRange;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.plugins.findbugs.util.model.Priority;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.ba.SourceFile;
import edu.umd.cs.findbugs.ba.SourceFinder;

/**
 * A parser for the native FindBugs XML files (ant task, batch file or
 * maven-findbugs-plugin >= 1.2-SNAPSHOT).
 */
public class NativeFindBugsParser {
    /** Determines whether we have access to the FindBugs library messages. */
    private static boolean isLibraryInitialized;

    static {
       initializeFindBugs();
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the native FindBugs format.
     * @param file
     *            the FindBugs analysis file
     * @param moduleRoot
     *            the root path of the maven module
     * @param moduleName
     *            name of maven module
     *
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException
     */
    public MavenModule parse(final InputStream file, final String moduleRoot, final String moduleName)
            throws IOException, DocumentException {
        Project project = createMavenProject(moduleRoot);

        SortedBugCollection collection = new SortedBugCollection();
        collection.readXML(file, project);

        SourceFinder sourceFinder = new SourceFinder();
        sourceFinder.setSourceBaseList(project.getSourceDirList());

        String actualName = extractModuleName(moduleName, project);
        MavenModule module = new MavenModule(actualName);

        Collection<BugInstance> bugs = collection.getCollection();
        for (BugInstance warning : bugs) {
            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();

            Bug bug;
            if (isLibraryInitialized) {
                bug = new Bug(getPriority(warning), getMessage(warning), getCategory(warning),
                        warning.getType(), sourceLine.getStartLine(), sourceLine.getEndLine(),
                        warning.getBugPattern().getDetailText());
            }
            else {
                bug = new Bug(getPriority(warning), getMessage(warning), getCategory(warning),
                        warning.getType(), sourceLine.getStartLine(), sourceLine.getEndLine());
            }

            Iterator<BugAnnotation> annotationIterator = warning.annotationIterator();
            while (annotationIterator.hasNext()) {
                BugAnnotation bugAnnotation = annotationIterator.next();
                if (bugAnnotation instanceof SourceLineAnnotation) {
                    SourceLineAnnotation annotation = (SourceLineAnnotation)bugAnnotation;
                    bug.addLineRange(new LineRange(annotation.getStartLine(), annotation.getEndLine()));
                }
            }
            String fileName;
            try {
                SourceFile sourceFile = sourceFinder.findSourceFile(sourceLine);
                fileName = sourceFile.getFullFileName();
            }
            catch (IOException exception) {
                fileName = sourceLine.getPackageName().replace(".", "/") + "/" + sourceLine.getSourceFile();
            }
            bug.setFileName(fileName);
            bug.setPackageName(warning.getPrimaryClass().getPackageName());
            bug.setModuleName(actualName);

            module.addAnnotation(bug);
        }
        return module;
    }

    /**
     * Returns the bug description message.
     *
     * @param warning
     *            the FindBugs warning
     * @return the bug description message.
     */
    private String getMessage(final BugInstance warning) {
        if (useNativeLibrary()) {
            return warning.getMessage();
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Returns if we should use the native library.
     *
     * @return <code>true</code> if we should use the native library.
     */
    private boolean useNativeLibrary() {
        return isLibraryInitialized;
    }

    /**
     * Maps the FindBugs priority to our priority enumeration.
     *
     * @param warning
     *            the FindBugs warning
     * @return mapped priority enumeration
     */
    private Priority getPriority(final BugInstance warning) {
        switch (warning.getPriority()) {
            case 1:
                return Priority.HIGH;
            case 2:
                return Priority.NORMAL;
            default:
                return Priority.LOW;
        }
    }

    /**
     * Extracts the module name from the specified project. If empty then the
     * provided default name is used.
     *
     * @param defaultName
     *            the default module name to use
     * @param project
     *            the maven 2 project
     * @return the module name to use
     */
    private String extractModuleName(final String defaultName, final Project project) {
        if (StringUtils.isBlank(project.getProjectName())) {
            return defaultName;
        }
        else {
            return project.getProjectName();
        }
    }

    /**
     * Returns the category of the bug.
     *
     * @param warning
     *            the bug
     * @return the category as a string
     */
    private String getCategory(final BugInstance warning) {
        if (useNativeLibrary()) {
            BugPattern bugPattern = warning.getBugPattern();
            if (bugPattern == null) {
                return "Unknown";
            }
            else {
                return bugPattern.getCategory();
            }
        }
        else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Initializes the FindBugs library.
     */
    private static void initializeFindBugs() {
        try {
            File findBugsJar = jarFile(DetectorFactoryCollection.class);
            String path = findBugsJar.getCanonicalPath();
            File core = new File(path
                    .replace("findbugs-", "coreplugin-")
                    .replace("findbugs/findbugs", "findbugs/coreplugin")
                    .replace("findbugs\\findbugs", "findbugs\\coreplugin"));

            DetectorFactoryCollection.rawInstance().setPluginList(new URL[] {core.toURL()});
            isLibraryInitialized = true;
        }
        catch (IOException exception) { // FIXME: provide a way to safely check for the existence of the library
            isLibraryInitialized = false;
        }
    }

    /**
     * Creates a maven project with some predefined source paths.
     *
     * @param moduleRoot
     *            the root path of the maven module
     * @return the new project
     */
    private Project createMavenProject(final String moduleRoot) {
        Project project = new Project();
        project.addSourceDir(moduleRoot + "/src/main/java");
        project.addSourceDir(moduleRoot + "/src/test/java");
        project.addSourceDir(moduleRoot + "/src");
        return project;
    }

    // FIXME: code is copied from Hudsons Which class
    /**
     * Locates the jar file that contains the given class.
     *
     * @throws IllegalArgumentException
     *      if failed to determine.
     */
    public static File jarFile(final Class clazz) throws IOException {
        ClassLoader cl = clazz.getClassLoader();
        if(cl==null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        URL res = cl.getResource(clazz.getName().replace('.', '/') + ".class");
        if(res==null) {
            throw new IllegalArgumentException("Unable to locate class file for "+clazz);
        }
        String resURL = res.toExternalForm();
        String originalURL = resURL;
        if(resURL.startsWith("jar:")) {
            resURL = resURL.substring(4, resURL.lastIndexOf('!')); // cut off jar: and the file name portion
            return new File(decode(new URL(resURL).getPath()));
        }

        if(resURL.startsWith("file:")) {
            // unpackaged classes
            int n = clazz.getName().split("\\.").length; // how many slashes do wo need to cut?
            for( ; n>0; n-- ) {
                int idx = Math.max(resURL.lastIndexOf('/'), resURL.lastIndexOf('\\'));
                if(idx<0) {
                    throw new IllegalArgumentException(resURL);
                }
                resURL = resURL.substring(0,idx);
            }

            // won't work if res URL contains ' '
            // return new File(new URI(null,new URL(res).toExternalForm(),null));
            // won't work if res URL contains '%20'
            // return new File(new URL(res).toURI());

            return new File(decode(new URL(resURL).getPath()));
        }

        throw new IllegalArgumentException(originalURL);
    }

    /**
     * Decode '%HH'.
     */
    private static String decode(final String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for( int i=0; i<s.length();i++ ) {
            char ch = s.charAt(i);
            if(ch=='%') {
                baos.write(hexToInt(s.charAt(i+1))*16 + hexToInt(s.charAt(i+2)));
                i+=2;
                continue;
            }
            baos.write(ch);
        }
        try {
            return new String(baos.toByteArray(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e); // impossible
        }
    }

    private static int hexToInt(final int ch) {
        return Character.getNumericValue(ch);
    }
}

