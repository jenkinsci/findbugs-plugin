package hudson.plugins.findbugs.parser.maven;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Scans the workspace and finds all Java files.
 *
 * @author Ulli Hafner
 */
public class JavaFileFinder implements FileCallable<String[]> {
    /** Generated ID. */
    private static final long serialVersionUID = 2970029366847565970L;

    /**
     * Returns an array with the filenames of the Java files that have been
     * found in the workspace.
     *
     * @param workspace
     *            root directory of the workspace
     * @param channel
     *            not used
     * @return the filenames of the FindBugs files
     */
    public String[] invoke(final File workspace, final VirtualChannel channel) throws IOException {
        FileSet fileSet = new FileSet();
        Project antProject = new Project();
        fileSet.setProject(antProject);
        fileSet.setDir(workspace);
        fileSet.setIncludes("**/*.java");

        return fileSet.getDirectoryScanner(antProject).getIncludedFiles();
    }
}