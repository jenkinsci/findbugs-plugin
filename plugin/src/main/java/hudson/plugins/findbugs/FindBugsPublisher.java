package hudson.plugins.findbugs;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;

import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Run;

import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwarePublisher;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.findbugs.parser.FindBugsParser;

/**
 * Publishes the results of the FindBugs analysis (freestyle project type).
 *
 * @author Ulli Hafner
 */
public class FindBugsPublisher extends HealthAwarePublisher {
    private static final long serialVersionUID = -5748362182226609649L;

    private static final String PLUGIN_NAME = "FINDBUGS";

    private static final String ANT_DEFAULT_PATTERN = "**/findbugs.xml";
    private static final String MAVEN_DEFAULT_PATTERN = "**/findbugsXml.xml";

    /** Ant file-set pattern of files to work with. */
    private String pattern;

    /** Determines whether to use the rank when evaluation the priority. @since 4.26 */
    private boolean isRankActivated;

    /** RegEx patterns of files to exclude from the report. */
    private String excludePattern;

    /** RegEx patterns of files to include in the report. */
    private String includePattern;

    /**
     * Creates a new instance of {@link FindBugsPublisher}.
     *
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
     * @param unstableTotalAll
     *            annotation threshold
     * @param unstableTotalHigh
     *            annotation threshold
     * @param unstableTotalNormal
     *            annotation threshold
     * @param unstableTotalLow
     *            annotation threshold
     * @param unstableNewAll
     *            annotation threshold
     * @param unstableNewHigh
     *            annotation threshold
     * @param unstableNewNormal
     *            annotation threshold
     * @param unstableNewLow
     *            annotation threshold
     * @param failedTotalAll
     *            annotation threshold
     * @param failedTotalHigh
     *            annotation threshold
     * @param failedTotalNormal
     *            annotation threshold
     * @param failedTotalLow
     *            annotation threshold
     * @param failedNewAll
     *            annotation threshold
     * @param failedNewHigh
     *            annotation threshold
     * @param failedNewNormal
     *            annotation threshold
     * @param failedNewLow
     *            annotation threshold
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     * @param usePreviousBuildAsReference
     *            determines whether to always use the previous build as the reference build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as reference builds or not
     * @param shouldDetectModules
     *            determines whether module names should be derived from Maven
     *            POM or Ant build files
     * @param pattern
     *            Ant file-set pattern to scan for FindBugs files
     * @param isRankActivated
     *            determines whether to use the rank when evaluation the
     *            priority
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     * @param excludePattern
     *            RegEx patterns of files to exclude from the report
     * @param includePattern
     *            RegEx patterns of files to include in the report
     * @deprecated This constructor is called internally only, but if you need to use it (for some strange reason), call
     *            {@link #FindBugsPublisher()} and available setters
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @Deprecated
    public FindBugsPublisher(final String healthy, final String unHealthy, final String thresholdLimit,
            final String defaultEncoding, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference, final boolean shouldDetectModules,
            final String pattern, final boolean isRankActivated, final boolean canComputeNew, final String excludePattern, final String includePattern) {
        super(healthy, unHealthy, thresholdLimit, defaultEncoding, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, usePreviousBuildAsReference, useStableBuildAsReference, shouldDetectModules, canComputeNew, false, PLUGIN_NAME);
        this.pattern = pattern;
        this.isRankActivated = isRankActivated;
        this.excludePattern= excludePattern;
        this.includePattern= includePattern;
    }
    // CHECKSTYLE:ON

    /**
     * Default data bound constructor.
     * Use setters to initialize the object if needed.
     */
    @DataBoundConstructor
    public FindBugsPublisher() {
        super(PLUGIN_NAME);
    }

    /**
     * Returns whether to use the rank when evaluation the priority.
     *
     * @return <code>true</code> if the rank should uses when evaluation the
     *         priority, <code>false</code> if the FindBugs priority should be
     *         used
     */
    public boolean isRankActivated() {
        return isRankActivated;
    }

    /**
     * Added to properly uncoercing.
     */
    public boolean isIsRankActivated() {
        return isRankActivated;
    }

    /**
     * @see {@link #isRankActivated()}
     */
    @DataBoundSetter
    public void setIsRankActivated(boolean isRankActivated) {
        this.isRankActivated = isRankActivated;
    }

    /**
     * Returns the Ant file-set pattern of files to work with.
     *
     * @return Ant file-set pattern of files to work with
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @see {@link #getPattern()}
     */
    @DataBoundSetter
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * RegEx patterns of files to exclude from the report.
     *
     * @return String of concatenated exclude patterns separated by a comma
     */
    public String getExcludePattern() {
        return excludePattern;
    }

    /**
     * @see {@link #getExcludePattern()}
     */
    @DataBoundSetter
    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
     * Returns the RegEx patterns to include in the report.
     *
     * @return String of concatenated include patterns separated by a comma
     */
    public String getIncludePattern() {
        return includePattern;
    }

    /**
     * @see {@link #getIncludePattern()}
     */
    @DataBoundSetter
    public void setIncludePattern(String includePattern) {
        this.includePattern = includePattern;
    }

    @Override
    public Action getProjectAction(final AbstractProject<?, ?> project) {
        return new FindBugsProjectAction(project);
    }

    @Override
    public BuildResult perform(final Run<?, ?> build, final FilePath workspace, final PluginLogger logger) throws InterruptedException, IOException {
        logger.log("Collecting findbugs analysis files...");

        String defaultPattern = ANT_DEFAULT_PATTERN;
        boolean isMavenBuild = isMavenBuild(build);
        if (isMavenBuild) {
            defaultPattern = MAVEN_DEFAULT_PATTERN;
        }
        FilesParser collector = new FilesParser(PLUGIN_NAME, StringUtils.defaultIfEmpty(getPattern(), defaultPattern),
                new FindBugsParser(isRankActivated, getExcludePattern(), getIncludePattern()), shouldDetectModules(), isMavenBuild);

        ParserResult project = workspace.act(collector);
        logger.logLines(project.getLogMessages());
        FindBugsResult result = new FindBugsResult(build, getDefaultEncoding(), project,
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());

        build.addAction(new FindBugsResultAction(build, this, result));

        return result;
    }

    @Override
    public FindBugsDescriptor getDescriptor() {
        return (FindBugsDescriptor)super.getDescriptor();
    }

    @Override
    public MatrixAggregator createAggregator(final MatrixBuild build, final Launcher launcher,
            final BuildListener listener) {
        return new FindBugsAnnotationsAggregator(build, launcher, listener, this, getDefaultEncoding(),
                usePreviousBuildAsReference(), useOnlyStableBuildsAsReference());
    }
}
