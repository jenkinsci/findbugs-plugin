package hudson.plugins.findbugs.parser;

import hudson.model.Hudson;
import hudson.plugins.findbugs.model.LineRange;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
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
    /** Determines whether the FindBugs library already has been initialized. */
    private static boolean isInitialized;
    /** Determines whether we should use the FindBugs library to create messages. */
    private static boolean useLibraryMessages;

    /**
     * Creates a new instance of <code>NativeFindBugsParser</code>.
     */
    public NativeFindBugsParser() {
        initializeFindBugs();
    }

    /**
     * Creates a new instance of <code>NativeFindBugsParser</code>.
     */
    public NativeFindBugsParser(final boolean useLibraryMessages) {
        if (useLibraryMessages) {
            initializeFindBugs();
        }
        else {
            isInitialized = true;
            NativeFindBugsParser.useLibraryMessages = false;
        }
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

        HashMap<String, WorkspaceFile> fileMapping = new HashMap<String, WorkspaceFile>();
        Collection<BugInstance> bugs = collection.getCollection();
        for (BugInstance warning : bugs) {
            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();

            Bug bug = new Bug(getPriority(warning), getMessage(warning), getCategory(warning), warning.getType(),
                    sourceLine.getStartLine(), sourceLine.getEndLine());

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
            if (!fileMapping.containsKey(fileName)) {
                WorkspaceFile workspaceFile = new WorkspaceFile();
                workspaceFile.setPackageName(warning.getPrimaryClass().getPackageName());
                workspaceFile.setModuleName(actualName);
                workspaceFile.setName(fileName);
                fileMapping.put(fileName, workspaceFile);
            }
            WorkspaceFile workspaceFile = fileMapping.get(fileName);
            workspaceFile.addAnnotation(bug);
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
        if (useLibraryMessages) {
            return warning.getMessage();
        }
        else {
            return StringUtils.EMPTY;
        }
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
        if (useLibraryMessages) {
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
    private static synchronized void initializeFindBugs() {
        if (!isInitialized) {
            File core = new File(Hudson.getInstance().getRootDir(),
                    "plugins/findbugs/WEB-INF/lib/coreplugin-1.2.0.jar");

            try {
                DetectorFactoryCollection.rawInstance().setPluginList(new URL[] {core.toURL()});
                useLibraryMessages = true;
            }
            catch (MalformedURLException exception) {
                useLibraryMessages = false;
            }
            isInitialized = true;
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

