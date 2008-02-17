package hudson.plugins.findbugs.parser.ant;

import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;
import hudson.plugins.findbugs.parser.Bug;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.dom4j.DocumentException;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.ba.SourceFile;
import edu.umd.cs.findbugs.ba.SourceFinder;

/**
 * A parser for the native FindBugs XML files (ant task).
 */
public class NativeFindBugsParser {
    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the native FindBugs format.
     *
     * @param file
     *            the FindBugs analysis file
     * @param moduleName
     *            name of maven module
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException
     */
    public MavenModule parse(final InputStream file, final String moduleName) throws IOException, DocumentException {
        SortedBugCollection collection = new SortedBugCollection();

        Project project = new Project();
        collection.readXML(file, project);

        Collection<BugInstance> bugs = collection.getCollection();

        MavenModule module = new MavenModule(moduleName);

        SourceFinder sourceFinder = new SourceFinder();
        sourceFinder.setSourceBaseList(project.getSourceDirList());

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

            Bug bug;
            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();
            bug = new Bug(priority, warning.getMessage(), warning.getMessage(), warning.getType(), sourceLine.getStartLine());

            String fileName;
            try {
                SourceFile sourceFile = sourceFinder.findSourceFile(sourceLine);
                fileName = sourceFile.getFullFileName();
            }
            catch (IOException exception) {
                fileName = "";
            }
            if (!fileMapping.containsKey(fileName)) {
                WorkspaceFile workspaceFile = new WorkspaceFile();
                workspaceFile.setPackageName(warning.getPrimaryClass().getPackageName());
                workspaceFile.setModuleName(moduleName);
                workspaceFile.setName(fileName);
                fileMapping.put(fileName, workspaceFile);
            }
            WorkspaceFile workspaceFile = fileMapping.get(fileName);
            workspaceFile.addAnnotation(bug);
            module.addAnnotation(bug);
        }
        //mapNativeWarnings2Files(module, collection.getProjectInformation());
        return module;
    }

    /**
     * Creates a mapping of warnings to actual workspace files (for native
     * format). The result is persisted in the results folder.
     *
     * @param module
     *            module containing all the warnings
     * @param projectInformation
     *            additional project information
     */
    private void mapNativeWarnings2Files(final MavenModule module, final ProjectInformation projectInformation) {
        Set<String> paths = projectInformation.getSourcePaths();
        if (paths.size() > 1) {
            for (WorkspaceFile workspaceFile : module.getFiles()) {
                for (String path : paths) {
                    String actualPath = path + "/" + workspaceFile.getName();
                    java.io.File file = new java.io.File(actualPath.replace('!', '/'));
                    if (file.exists()) {
                        workspaceFile.setName(actualPath);
                        break;
                    }
                }
            }
        }
        else if (paths.size() == 1) {
            String prefix = paths.iterator().next();
            for (WorkspaceFile workspaceFile : module.getFiles()) {
                workspaceFile.setName(prefix + "/" + workspaceFile.getName());
            }
        }
    }
}

