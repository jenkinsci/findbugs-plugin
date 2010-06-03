package hudson.plugins.findbugs;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.findbugs.parser.FindBugsParser;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the FindBugs analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class FindBugsPublisher extends HealthAwarePublisher {
    private static final long serialVersionUID = -5748362182226609649L;

    private static final String ANT_DEFAULT_PATTERN = "**/findbugs.xml";
    private static final String MAVEN_DEFAULT_PATTERN = "**/findbugsXml.xml";

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
     * @param thresholdLimit
     *            determines which warning priorities should be considered when
     *            evaluating the build stability and health
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param useDeltaValues
     *            determines whether the absolute annotations delta or the
     *            actual annotations set difference should be used to evaluate
     *            the build stability
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public FindBugsPublisher(final String pattern, final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues, final boolean canRunOnFailed) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues, canRunOnFailed, "FINDBUGS");
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
        return new FindBugsProjectAction(project);
    }

    /** {@inheritDoc} */
    @Override
    public BuildResult perform(final AbstractBuild<?, ?> build, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting findbugs analysis files...");

        String defaultPattern = isMavenBuild(build) ? MAVEN_DEFAULT_PATTERN : ANT_DEFAULT_PATTERN;
        FilesParser collector = new FilesParser(logger, StringUtils.defaultIfEmpty(getPattern(), defaultPattern),
                new FindBugsParser(), isMavenBuild(build), isAntBuild(build));
        ParserResult project = build.getWorkspace().act(collector);
        FindBugsResult result = new FindBugsResult(build, getDefaultEncoding(), project);

        build.getActions().add(new FindBugsResultAction(build, this, result));

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public FindBugsDescriptor getDescriptor() {
        return (FindBugsDescriptor)super.getDescriptor();
    }

}
