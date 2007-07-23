package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Publishes the results of the FindBugs analysis.
 * <p>
 * TODO: Add checking of ant path like in the JUNIT plugin
 *
 * @author Ulli Hafner
 */
public class FindBugsPublisher extends Publisher {
    /** Descriptor of this publisher. */
    public static final FindBugsDescriptor FIND_BUGS_DESCRIPTOR = new FindBugsDescriptor();
    /** Ant file-set pattern to scan for FindBugs files. */
    private final String pattern;
    /** Bug threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private boolean isThresholdEnabled;
    /** Integer bug threshold to be reached if a build should be considered as unstable. */
    private int minimumBugs;

    /**
     * Creates a new instance of <code>FindBugsPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern to scan for FindBugs files
     * @param threshold
     *            Bug threshold to be reached if a build should be considered as
     *            unstable.
     * @stapler-constructor
     */
    public FindBugsPublisher(final String pattern, final String threshold) {
        super();
        this.threshold = threshold;
        this.pattern = StringUtils.defaultIfEmpty(pattern, "**/target/findbugs.xml");

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumBugs = Integer.valueOf(threshold);
                isThresholdEnabled = true;
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /**
     * Returns the Bug threshold to be reached if a build should be considered as unstable.
     *
     * @return the bug threshold
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Returns the Ant file-set pattern to FindBugs XML files.
     *
     * @return ant file-set pattern to FindBugs XML files.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Scans the workspace, collects all data files and copies these files to
     * the build results folder. Then counts the number of bugs and sets the
     * result of the build accordingly ({@link #threshold}.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @return true in case the processing has been aborted
     * @throws IOException
     *             if the files could not be copied
     * @throws InterruptedException
     *             if user cancels the operation
     */
    public boolean perform(final Build<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Collecting findbugs analysis files...");
        FilePath workingDirectory = createWorkingDirectory(build.getRootDir());

        if (!copyFilesFromWorkspaceToBuild(build, listener, workingDirectory)) {
            return true;
        }

        int warnings = findBugs(build, listener, workingDirectory);

        persistBuildReport(build, warnings);

        return false;
    }

    /**
     * Persists the state of this FindBugs analysis by creating a
     * {@link FindBugsResult} and attaching it to a {@link FindBugsBuildAction}.
     *
     * @param build the current build
     * @param warnings the number of found warnings
     */
    private void persistBuildReport(final Build<?, ?> build, final int warnings) {
        FindBugsBuildAction action = new FindBugsBuildAction(build);
        if (action.hasPreviousResult()) {
            FindBugsResult result = action.getPreviousResult().getResult();
            action.setResult(new FindBugsResult(warnings, result.getNumberOfWarnings()));
        }
        else {
            action.setResult(new FindBugsResult(warnings));
        }
        build.getActions().add(action);
    }

    /**
     * Finds the bugs reported in the FindBugs xml files.
     *
     * @param build
     *            the current build
     * @param listener
     *            the build listener
     * @param workingDirectory
     *            the working directory where the FindBugs files are stored
     * @return the number of warnings found
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if the user canceled the operation
     */
    private int findBugs(final Build<?, ?> build, final BuildListener listener,
            final FilePath workingDirectory) throws IOException, InterruptedException {
        FilePath[] list = workingDirectory.list("*.xml");
        int warnings = 0;
        for (FilePath filePath : list) {
            InputStream file = filePath.read();
            warnings += new FindBugsCounter().count(IOUtils.lineIterator(file, "UTF-8"));
        }
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " potential bugs have been found.");
            if (isThresholdEnabled && warnings >= minimumBugs) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No potential bugs have been found.");
        }
        return warnings;
    }

    /**
     * Copies the FindBugs files from the workspace to the build folder. If this
     * could not be done, then the build is marked as a failure.
     *
     * @param build
     *            the build
     * @param listener
     *            the build listener
     * @param buildFolder
     *            destination folder
     * @return <code>false</code> in case of an error
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if the user canceled the operation
     */
    private boolean copyFilesFromWorkspaceToBuild(final Build<?, ?> build, final BuildListener listener,
            final FilePath buildFolder) throws IOException, InterruptedException {
        try {
            build.getProject().getWorkspace().act(
                    new FindBugsCollector(listener, buildFolder, build.getTimestamp().getTimeInMillis(), pattern));
            return true;
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    /**
     * Creates the working directory where all reports will be copied to.
     *
     * @param rootDirectory root directory of the current build
     *
     * @return the created directory
     */
    private FilePath createWorkingDirectory(final File rootDirectory) {
        File dataDir =  new File(rootDirectory, "findbugs-results");
        dataDir.mkdirs();

        return new FilePath(dataDir);
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return FIND_BUGS_DESCRIPTOR;
    }
}
