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
    // FIXME: check whether this way is portable
    static {
        File core = new File(Hudson.getInstance().getRootDir(), "plugins/findbugs/WEB-INF/lib/coreplugin-1.2.0.jar");

        try {
            DetectorFactoryCollection.rawInstance().setPluginList(new URL[] {core.toURL()});
        }
        catch (MalformedURLException exception) {
            // ignore
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
        SortedBugCollection collection = new SortedBugCollection();


        Project project = createMavenProject(moduleRoot);

        collection.readXML(file, project);
        Collection<BugInstance> bugs = collection.getCollection();

        SourceFinder sourceFinder = new SourceFinder();
        sourceFinder.setSourceBaseList(project.getSourceDirList());

        String actualName;
        if (StringUtils.isBlank(project.getProjectName())) {
            actualName = moduleName;
        }
        else {
            actualName = project.getProjectName();
        }
        MavenModule module = new MavenModule(actualName);
        HashMap<String, WorkspaceFile> fileMapping = new HashMap<String, WorkspaceFile>();
        for (BugInstance warning : bugs) {
            Priority priority;
            switch (warning.getPriority()) {
                case 1:
                    priority = Priority.HIGH;
                    break;
                case 2:
                    priority = Priority.NORMAL;
                    break;
                default:
                    priority = Priority.LOW;
            }

            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();
            Bug bug = new Bug(priority, warning.getMessage(), warning.getBugPattern().getCategory(), warning.getType(),
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

