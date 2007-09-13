package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.findbugs.util.AbortException;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Publishes the results of the FindBugs analysis.
 *
 * @author Ulli Hafner
 */
public class FindBugsPublisher extends Publisher {
    /** Default findbugs pattern. */
    private static final String DEFAULT_PATTERN = "**/findbugs.xml";
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
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthyBugs;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthyBugs;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean isHealthyReportEnabled;

    /**
     * Creates a new instance of <code>FindBugsPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern to scan for FindBugs files
     * @param threshold
     *            Bug threshold to be reached if a build should be considered as
     *            unstable.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @stapler-constructor
     */
    public FindBugsPublisher(final String pattern, final String threshold,
            final String healthy, final String unHealthy) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.pattern = pattern;

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumBugs = Integer.valueOf(threshold);
                if (minimumBugs >= 0) {
                    isThresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (!StringUtils.isEmpty(healthy) && !StringUtils.isEmpty(unHealthy)) {
            try {
                healthyBugs = Integer.valueOf(healthy);
                unHealthyBugs = Integer.valueOf(unHealthy);
                if (healthyBugs >= 0 && unHealthyBugs > healthyBugs) {
                    isHealthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final Project project) {
        return new FindBugsProjectAction(project);
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
     * Returns the healthy threshold.
     *
     * @return the healthy
     */
    public String getHealthy() {
        return healthy;
    }

    /**
     * Returns the unhealthy threshold.
     *
     * @return the unHealthy
     */
    public String getUnHealthy() {
        return unHealthy;
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
        FindBugsCounter findBugsCounter = new FindBugsCounter(build);

        FilePath workingDirectory = findBugsCounter.getWorkingDirectory();
        workingDirectory.mkdirs();
        if (!copyFilesFromWorkspaceToBuild(build, listener, workingDirectory)) {
            return true;
        }


        try {
            JavaProject project = findBugsCounter.findBugs();

            Object previous = build.getPreviousBuild();
            FindBugsResult result;
            if (previous instanceof Build<?, ?>) {
                Build<?, ?> previousBuild = (Build<?, ?>)previous;
                FindBugsResultAction previousAction = previousBuild.getAction(FindBugsResultAction.class);
                if (previousAction == null) {
                    result = new FindBugsResult(build, project);
                }
                else {
                    result = new FindBugsResult(build, project, previousAction.getResult().getProject());
                }
            }
            else {
                result = new FindBugsResult(build, project);
            }

            HealthReportBuilder healthReportBuilder = new HealthReportBuilder("FindBugs", "warning", isThresholdEnabled, minimumBugs, isHealthyReportEnabled, healthyBugs, unHealthyBugs);
            build.getActions().add(new FindBugsResultAction(build, result, healthReportBuilder));

            int warnings = project.getNumberOfWarnings();
            if (warnings > 0) {
                listener.getLogger().println("A total of " + warnings + " potential bugs have been found.");
                if (isThresholdEnabled && warnings >= minimumBugs) {
                    build.setResult(Result.UNSTABLE);
                }
            }
            else {
                listener.getLogger().println("No potential bugs have been found.");
            }
        }
        catch (SAXException exception) {
            listener.getLogger().println();
            exception.printStackTrace(listener.fatalError("Could not parse FindBugs files. Please check if the file pattern is correct\nand the latest FindBugs scanner is used (i.e., maven-findbugs-plugin >= 1.1.1)"));
            build.setResult(Result.FAILURE);
            return true;
        }

        return false;
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
                    new FindBugsCollector(listener, buildFolder, build.getTimestamp().getTimeInMillis(),
                            StringUtils.defaultIfEmpty(pattern, DEFAULT_PATTERN)));
            return true;
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return FIND_BUGS_DESCRIPTOR;
    }
}
