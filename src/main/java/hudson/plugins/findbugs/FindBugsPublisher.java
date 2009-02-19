package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.plugins.findbugs.parser.FindBugsParser;
import hudson.plugins.findbugs.util.AnnotationsBuildResult;
import hudson.plugins.findbugs.util.FilesParser;
import hudson.plugins.findbugs.util.HealthAwarePublisher;
import hudson.plugins.findbugs.util.ParserResult;
import hudson.plugins.findbugs.util.PluginDescriptor;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the FindBugs analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class FindBugsPublisher extends HealthAwarePublisher {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -5748362182226609649L;
    /** Default FindBugs pattern. */
    private static final String DEFAULT_PATTERN = "**/findbugs.xml";
    /** Descriptor of this publisher. */
    public static final PluginDescriptor FIND_BUGS_DESCRIPTOR = new FindBugsDescriptor();
    /** Ant file-set pattern of files to work with. */
    private final String pattern;

    /**
     * Creates a new instance of <code>FindBugsPublisher</code>.
     *
     * @param pattern
     *            Ant file-set pattern to scan for FindBugs files
     * @param threshold
     *            Annotation threshold to be reached if a build should be considered as
     *            unstable.
     * @param newThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as unstable.
     * @param failureThreshold
     *            Annotation threshold to be reached if a build should be considered as
     *            failure.
     * @param newFailureThreshold
     *            New annotations threshold to be reached if a build should be
     *            considered as failure.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     * @param minimumPriority
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public FindBugsPublisher(final String pattern, final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy,
            final String height, final Priority minimumPriority, final String defaultEncoding) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, height, minimumPriority, defaultEncoding, "FINDBUGS");
        this.pattern = pattern;
    }
    // CHECKSTYLE:ON

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new FindBugsProjectAction(project, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationsBuildResult perform(final AbstractBuild<?, ?> build, final PrintStream logger) throws InterruptedException, IOException {
        log(logger, "Collecting findbugs analysis files...");
        FilesParser collector = new FilesParser(logger, StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN),
                new FindBugsParser(build.getProject().getWorkspace()),
                isMavenBuild(build), isAntBuild(build));
        ParserResult project = build.getProject().getWorkspace().act(collector);
        FindBugsResult result = new FindBugsResultBuilder().build(build, project, getDefaultEncoding());

        build.getActions().add(new FindBugsResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    public Descriptor<Publisher> getDescriptor() {
        return FIND_BUGS_DESCRIPTOR;
    }
}
