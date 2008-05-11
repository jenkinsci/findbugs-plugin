package hudson.plugins.findbugs.parser;

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

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import com.mebigfatguy.fbcontrib.FBContrib;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.ba.SourceFile;
import edu.umd.cs.findbugs.ba.SourceFinder;
import edu.umd.cs.findbugs.detect.DroppedException;

/**
 * A parser for the native FindBugs XML files (ant task, batch file or
 * maven-findbugs-plugin >= 1.2-SNAPSHOT).
 *
 * @author Ulli Hafner
 */
public class PlainFindBugsParser {
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

            Bug bug = new Bug(getPriority(warning), getMessage(warning), warning.getBugPattern().getCategory(),
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
     * Returns an URL array containing the references to the FindBugs libraries.
     *
     * @return URL array containing the references to the FindBugs libraries
     * @throws MalformedURLException
     */
    public static URL[] createPluginUrls() throws MalformedURLException {
        return new URL[] {getPluginPath(DroppedException.class), getPluginPath(FBContrib.class)};
    }

    /**
     * Returns the path to the JAR file containing the given path.
     *
     * @param clazz
     *            the class to get the JAR URL for
     * @return path to the FindBugs plug-in
     * @throws MalformedURLException
     *             if the guessed URL is not valid
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("REC")
    private static URL getPluginPath(final Class<?> clazz) throws MalformedURLException {
        try {
            return Which.jarFile(clazz).toURL();
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

