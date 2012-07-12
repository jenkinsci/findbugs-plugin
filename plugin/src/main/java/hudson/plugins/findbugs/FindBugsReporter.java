package hudson.plugins.findbugs;

import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwareReporter;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.util.PluginLogger;
import hudson.plugins.findbugs.parser.FindBugsParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Publishes the results of the FindBugs analysis (maven 2 project type).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class FindBugsReporter extends HealthAwareReporter<FindBugsResult> {
    private static final long serialVersionUID = -288391908253344862L;

    private static final String PLUGIN_NAME = "FINDBUGS";

    /** FindBugs filename if maven findbugsXmlOutput is activated. */
    private static final String FINDBUGS_XML_FILE = "findbugsXml.xml";
    /** FindBugs filename if maven findbugsXmlOutput is not activated. */
    private static final String MAVEN_FINDBUGS_XML_FILE = "findbugs.xml";

    /** Determines whether to use the rank when evaluation the priority. @since 4.26 */
    private final boolean isRankActivated;

    /**
     * Creates a new instance of <code>FindBugsReporter</code>.
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
     * @param isRankActivated
     *            determines whether to use the rank when evaluation the
     *            priority
     * @param canComputeNew
     *            determines whether new warnings should be computed (with
     *            respect to baseline)
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public FindBugsReporter(final String healthy, final String unHealthy, final String thresholdLimit, final boolean useDeltaValues,
            final String unstableTotalAll, final String unstableTotalHigh, final String unstableTotalNormal, final String unstableTotalLow,
            final String unstableNewAll, final String unstableNewHigh, final String unstableNewNormal, final String unstableNewLow,
            final String failedTotalAll, final String failedTotalHigh, final String failedTotalNormal, final String failedTotalLow,
            final String failedNewAll, final String failedNewHigh, final String failedNewNormal, final String failedNewLow,
            final boolean canRunOnFailed, final boolean isRankActivated, final boolean canComputeNew) {
        super(healthy, unHealthy, thresholdLimit, useDeltaValues,
                unstableTotalAll, unstableTotalHigh, unstableTotalNormal, unstableTotalLow,
                unstableNewAll, unstableNewHigh, unstableNewNormal, unstableNewLow,
                failedTotalAll, failedTotalHigh, failedTotalNormal, failedTotalLow,
                failedNewAll, failedNewHigh, failedNewNormal, failedNewLow,
                canRunOnFailed, canComputeNew, PLUGIN_NAME);
        this.isRankActivated = isRankActivated;
    }
    // CHECKSTYLE:ON

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

    @Override
    public boolean preExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final BuildListener listener) throws InterruptedException, IOException {
        if ("findbugs".equals(mojo.getGoal())) {
            activateProperty(mojo, "xmlOutput");
            activateProperty(mojo, "findbugsXmlOutput");
        }
        return true;
    }


    /**
     * Activates the specified property of the mojo.
     *
     * @param mojo
     *            the mojo to change
     * @param property
     *            the property toset to <code>true</code>
     */
    private void activateProperty(final MojoInfo mojo, final String property) {
        XmlPlexusConfiguration configuration = (XmlPlexusConfiguration) mojo.configuration.getChild(property);
        if (configuration != null) {
            configuration.setValue("true");
        }
    }

    @Override
    protected boolean acceptGoal(final String goal) {
        return "findbugs".equals(goal) || "site".equals(goal);
    }

    @Override
    public ParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final PluginLogger logger) throws InterruptedException, IOException {
        List<String> sources = new ArrayList<String>(pom.getCompileSourceRoots());
        sources.addAll(pom.getTestCompileSourceRoots());

        FilesParser findBugsCollector = new FilesParser(PLUGIN_NAME, determineFileName(mojo),
                new FindBugsParser(sources, isRankActivated), getModuleName(pom));

        return getTargetPath(pom).act(findBugsCollector);
    }

    @Override
    protected FindBugsResult createResult(final MavenBuild build, final ParserResult project) {
        return new FindBugsReporterResult(build, getDefaultEncoding(), project);
    }

    @Override
    protected MavenAggregatedReport createMavenAggregatedReport(final MavenBuild build, final FindBugsResult result) {
        return new FindBugsMavenResultAction(build, this, getDefaultEncoding(), result);
    }

    /**
     * Determines the filename of the FindBugs results.
     *
     * @param mojo
     *            the mojo containing the FindBugs configuration
     * @return filename of the FindBugs results
     */
    private String determineFileName(final MojoInfo mojo) {
        try {
            if (FindBugsPlugin.isFindBugs2x(mojo.mojoExecution)) {
                return FINDBUGS_XML_FILE;
            }

            Boolean isNativeFormat = mojo.getConfigurationValue("findbugsXmlOutput", Boolean.class);
            if (Boolean.FALSE.equals(isNativeFormat)) {
                return MAVEN_FINDBUGS_XML_FILE;
            }
        }
        catch (ComponentConfigurationException exception) {
            // return new format
        }
        return FINDBUGS_XML_FILE;
    }

    @Override
    public List<FindBugsProjectAction> getProjectActions(final MavenModule module) {
        return Collections.singletonList(new FindBugsProjectAction(module, getResultActionClass()));
    }

    @Override
    protected Class<FindBugsMavenResultAction> getResultActionClass() {
        return FindBugsMavenResultAction.class;
    }

    /** Ant file-set pattern of files to work with. @deprecated */
    @SuppressWarnings("PMD")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @Deprecated
    private transient String pattern; // obsolete since release 2.5
}

