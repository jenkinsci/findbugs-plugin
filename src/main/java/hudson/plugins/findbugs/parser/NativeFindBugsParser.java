package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.FindBugsResult;
import hudson.plugins.findbugs.util.model.LineRange;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.remoting.Which;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

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
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FindBugsResult.class.getName());
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

            Bug bug = new Bug(getPriority(warning), getMessage(warning), getCategory(warning),
                        warning.getType(), sourceLine.getStartLine(), sourceLine.getEndLine(),
                        warning.getBugPattern().getDetailText());

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
        return warning.getMessage();
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
        BugPattern bugPattern = warning.getBugPattern();
        if (bugPattern == null) {
            return "Unknown";
        }
        else {
            return bugPattern.getCategory();
        }
    }

    /**
     * Initializes the FindBugs library.
     */
    private static void initializeFindBugs() {
        try {
            String path = System.getProperty("hudson.plugins.findbugs.pluginpath");
            if (path != null) {
                String[] urlStrings = StringUtils.split(path, ";");
                URL[] urls = new URL[urlStrings.length];

                for (int i = 0; i < urlStrings.length; i++) {
                    urls[i] = new URL(urlStrings[i]);
                }
                DetectorFactoryCollection.rawInstance().setPluginList(urls);
            }
            else {
                URL pluginPath = getPluginPath();
                String original = pluginPath.toString().replace("\\", "/");
                String urlName = StringUtils.substringBeforeLast(original, "/") + "/fbcontrib-3.4.2-hudson-1.jar";

                DetectorFactoryCollection.rawInstance().setPluginList(new URL[] {pluginPath, new URL(urlName)});
            }
        }
        catch (MalformedURLException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Returns the path to the FindBugs plug-in.
     *
     * @return path to the FindBugs plug-in
     * @throws MalformedURLException
     *             if the guessed URL is not valid
     */
    private static URL getPluginPath() throws MalformedURLException {
        try {
            return Which.jarFile(DetectorFactoryCollection.class).toURL();
        }
        catch (Exception exception) {
            return new URL("file://");
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
}

