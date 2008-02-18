package hudson.plugins.findbugs;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.plugins.findbugs.model.JavaProject;
import hudson.plugins.findbugs.util.AbortException;
import hudson.plugins.findbugs.util.HealthAwarePublisher;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.tasks.Publisher;

import java.io.IOException;

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

        JavaProject project;
        try {
            project = build.getProject().getWorkspace().act(
                    new FindBugsCollector(listener, build.getTimestamp().getTimeInMillis(),
                            StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN)));
        }
        catch (AbortException exception) {
            listener.getLogger().println(exception.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }

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

        int warnings = project.getNumberOfAnnotations();
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " potential bugs have been found.");
            if (isThresholdEnabled() && warnings >= getMinimumAnnotations()) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No potential bugs have been found.");
        }

        return true;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return FIND_BUGS_DESCRIPTOR;
    }
}
