package hudson.plugins.findbugs;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.findbugs.model.JavaProject;
import hudson.plugins.findbugs.parser.FindBugsCollector;
import hudson.plugins.findbugs.util.AbortException;
import hudson.plugins.findbugs.util.HealthAwarePublisher;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;

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
    public FindBugsPublisher(final String pattern, final String threshold, final String healthy, final String unHealthy) {
        super(pattern, threshold, healthy, unHealthy);
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new FindBugsProjectAction(project);
    }

    /**
     * Scans the workspace and parses all found FindBugs XML files. Then, the
     * annotations of all files are merged and persisted for a build. Finally,
     * the number of bugs are counted and the result of the build is set
     * accordingly ({@link #getThreshold()}.
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
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher,
            final BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        try {
            logger.println("Collecting findbugs analysis files...");

            JavaProject project = parseAllWorkspaceFiles(build, logger);
            FindBugsResult result = createResult(build, project);

            HealthReportBuilder healthReportBuilder = createHealthReporter("FindBugs", "warning");
            build.getActions().add(new FindBugsResultAction(build, result, healthReportBuilder));

            evaluateBuildResult(build, logger, project);

            return true;
        }
        catch (AbortException exception) {
            logger.println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
    }

    /**
     * Evaluates the build result. The build is marked as unstable if the
     * threshold has been exceeded.
     *
     * @param build
     *            the build to create the action for
     * @param logger
     *            the logger
     * @param project
     *            the project with the annotations
     */
    private void evaluateBuildResult(final AbstractBuild<?, ?> build, final PrintStream logger, final JavaProject project) {
        int warnings = project.getNumberOfAnnotations();
        if (warnings > 0) {
            logger.println(
                    "A total of " + warnings + " potential bugs have been found.");
            if (isThresholdEnabled() && warnings >= getMinimumAnnotations()) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            logger.println("No potential bugs have been found.");
        }
    }

    /**
     * Scans the workspace for FindBugs files matching the specified pattern and
     * returns all found annotations merged in a project.
     *
     * @param build
     *            the build to create the action for
     * @param logger
     *            the logger
     * @return the project with the annotations
     * @throws IOException
     *             if the files could not be read
     * @throws InterruptedException
     *             if user cancels the operation
     */
    private JavaProject parseAllWorkspaceFiles(final AbstractBuild<?, ?> build,
            final PrintStream logger) throws IOException, InterruptedException {
        FindBugsCollector findBugsCollector = new FindBugsCollector(logger, build.getTimestamp().getTimeInMillis(),
                        StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN));

        return build.getProject().getWorkspace().act(findBugsCollector);
    }

    /**
     * Creates a result that persists the FindBugs information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @return the result action
     */
    private FindBugsResult createResult(final AbstractBuild<?, ?> build, final JavaProject project) {
        Object previous = build.getPreviousBuild();
        FindBugsResult result;
        if (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            FindBugsResultAction previousAction = previousBuild.getAction(FindBugsResultAction.class);
            if (previousAction == null) {
                result = new FindBugsResult(build, project);
            }
            else {
                result = new FindBugsResult(build, project, previousAction.getResult().getProject(),
                        previousAction.getResult().getZeroWarningsHighScore());
            }
        }
        else {
            result = new FindBugsResult(build, project);
        }
        return result;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return FIND_BUGS_DESCRIPTOR;
    }
}
