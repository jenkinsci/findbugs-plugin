package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.findbugs.util.AbortException;
import hudson.plugins.findbugs.util.HealthAwarePublisher;
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
public class FindBugsPublisher extends HealthAwarePublisher {
    /** Default FindBugs pattern. */
    private static final String DEFAULT_PATTERN = "**/findbugs.xml";
    /** Descriptor of this publisher. */
    public static final FindBugsDescriptor FIND_BUGS_DESCRIPTOR = new FindBugsDescriptor();

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
        super(pattern, threshold, healthy, unHealthy);
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new FindBugsProjectAction(project);
    }

    /**
     * Scans the workspace, collects all data files and copies these files to
     * the build results folder. Then counts the number of bugs and sets the
     * result of the build accordingly ({@link #getThreshold()}.
     *
     * @param build
     *            the build
     * @param launcher
     *            the launcher
     * @param listener
     *            the build listener
     * @return <code>true</code> if the build could continue
     * @throws IOException
     *             if the files could not be copied
     * @throws InterruptedException
     *             if user cancels the operation
     */
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        listener.getLogger().println("Collecting findbugs analysis files...");
        FindBugsCounter findBugsCounter = new FindBugsCounter(build);

        FilePath workingDirectory = findBugsCounter.getWorkingDirectory();
        workingDirectory.mkdirs();
        if (!copyFilesFromWorkspaceToBuild(build, listener, workingDirectory)) {
            return false;
        }

        try {
            JavaProject project = findBugsCounter.findBugs();
            findBugsCounter.mapWarnings2Files(project);

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

            HealthReportBuilder healthReportBuilder = createHealthReporter("FindBugs", "warning");
            build.getActions().add(new FindBugsResultAction(build, result, healthReportBuilder));

            int warnings = project.getNumberOfWarnings();
            if (warnings > 0) {
                listener.getLogger().println("A total of " + warnings + " potential bugs have been found.");
                if (isThresholdEnabled() && warnings >= getMinimumAnnotations()) {
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
            return false;
        }

        return true;
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
    private boolean copyFilesFromWorkspaceToBuild(final AbstractBuild<?,?> build, final BuildListener listener,
            final FilePath buildFolder) throws IOException, InterruptedException {
        try {
            build.getProject().getWorkspace().act(
                    new FindBugsCollector(listener, buildFolder, build.getTimestamp().getTimeInMillis(),
                            StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN)));
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
