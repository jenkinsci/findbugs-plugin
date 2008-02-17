package hudson.plugins.findbugs.parser;

import hudson.FilePath.FileCallable;
import hudson.plugins.findbugs.model.JavaProject;
import hudson.plugins.findbugs.model.WorkspaceFile;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.types.FileSet;

/**
 * Scans the workspace module and maps Java files to classes.
 *
 * @author Ulli Hafner
 */
class WorkspaceScanner implements FileCallable<Void> {
    /** Generated ID. */
    private static final long serialVersionUID = 2970029366847565970L;
    /** FindBugs result. */
    private final JavaProject project;

    /**
     * Creates a new instance of <code>WorkspaceScanner</code>.
     *
     * @param project
     *            the FindBugs result
     */
    public WorkspaceScanner(final JavaProject project) {
        this.project = project;
    }

    /** {@inheritDoc} */
    public Void invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] files = findJavaFiles(workspace);
        HashMap<String, String> fileMapping = new HashMap<String, String>();
        for (int i = 0; i < files.length; i++) {
            String name = files[i].replace('/', '.').replace('\\', '.');
            if (name.contains(".src.main.java.")) {
                String key = StringUtils.substringAfterLast(name, "src.main.java.");
                fileMapping.put(key, files[i]);
            }
            else if (name.contains(".src.")) {
                String key = StringUtils.substringAfterLast(name, "src.");
                fileMapping.put(key, files[i]);
            }
        }
        for (WorkspaceFile file : project.getFiles()) {
            String key = StringUtils.substringBeforeLast(file.getPackageName() + "." + file.getName(), "$") + ".java";
            if (fileMapping.containsKey(key)) {
                file.setName(fileMapping.get(key));
             }
        }

        return null;
    }

    /**
     * Returns an array with the filenames of the Java files that have been found in
     * the workspace.
     *
     * @param workspaceRoot
     *            root directory of the workspace
     * @return the filenames of the FindBugs files
     */
    private String[] findJavaFiles(final File workspaceRoot) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project antProject = new org.apache.tools.ant.Project();
        fileSet.setProject(antProject);
        fileSet.setDir(workspaceRoot);
        fileSet.setIncludes("**/*.java");

        return fileSet.getDirectoryScanner(antProject).getIncludedFiles();
    }
}