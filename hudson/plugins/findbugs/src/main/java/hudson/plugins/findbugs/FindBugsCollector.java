package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.types.FileSet;

/**
 * Collects the FindBugs analysis files and copies them to the working directory.
 */
class FindBugsCollector implements FileCallable<Void> {
    /** Determines whether to skip old files. */
    private static final boolean SKIP_OLD_FILES = false;

    /** Logger. */
    private final transient BuildListener listener;

    /** Working directory to copy results to. */
    private final FilePath workingDirectory;

    /** Build time stamp, only newer files are considered. */
    private final long buildTime;

    /** Ant file-set pattern to scan for FindBugs files. */
    private final String filePattern;

    /** Generated ID. */
    private static final long serialVersionUID = -6415863872891783891L;

    /**
     * Creates a new instance of <code>FindBugsCollector</code>.
     *
     * @param listener the Logger
     * @param workingDirectory working directory to copy results to
     * @param buildTime build time stamp, only newer files are considered
     * @param filePattern ant file-set pattern to scan for FindBugs files
     */
    FindBugsCollector(final BuildListener listener, final FilePath workingDirectory, final long buildTime, final String filePattern) {
        this.listener = listener;
        this.workingDirectory = workingDirectory;
        this.buildTime = buildTime;
        this.filePattern = filePattern;
    }

    /** {@inheritDoc} */
    public Void invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] findBugsFiles = findFindBugsFiles(workspace);
        if (findBugsFiles.length == 0) {
            throw new AbortException("No findbugs report files were found. Configuration error?");
        }

        int counter = 0;
        for (String file : findBugsFiles) {
            File originalFile = new File(workspace, file);

            if (SKIP_OLD_FILES && originalFile.lastModified() < buildTime) {
                listener.getLogger().println("Skipping " + originalFile + " because it's not up to date");
            }
            else {
                try {
                    new FilePath(originalFile).copyTo(workingDirectory.child("report-" + counter + ".xml"));
                }
                catch (InterruptedException exception) {
                    throw new IOException2("Aborted while copying " + originalFile, exception);
                }
            }
            counter++;
        }
        return null;
    }

    /**
     * Returns an array with the filenames of the FindBugs files that have
     * been found in the workspace.
     *
     * @param workspaceRoot root directory of the workspace
     *
     * @return the filenames of the FindBugs files
     */
    private String[] findFindBugsFiles(final File workspaceRoot) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(workspaceRoot);
        fileSet.setIncludes(filePattern);

        return fileSet.getDirectoryScanner(project).getIncludedFiles();
    }
}