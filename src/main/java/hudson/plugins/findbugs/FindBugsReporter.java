package hudson.plugins.findbugs;

import hudson.maven.MavenBuildProxy;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.FilesParser;
import hudson.plugins.analysis.core.HealthAwareMavenReporter;
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
public class FindBugsReporter extends HealthAwareMavenReporter {
    private static final long serialVersionUID = -288391908253344862L;

    /** FindBugs filename if maven findbugsXmlOutput is activated. */
    private static final String FINDBUGS_XML_FILE = "findbugsXml.xml";
    /** FindBugs filename if maven findbugsXmlOutput is not activated. */
    private static final String MAVEN_FINDBUGS_XML_FILE = "findbugs.xml";

    /**
     * Creates a new instance of <code>FindBugsReporter</code>.
     *
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
     * @param canRunOnFailed
     *            determines whether the plug-in can run for failed builds, too
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    @DataBoundConstructor
    public FindBugsReporter(final String threshold, final String newThreshold,
            final String failureThreshold, final String newFailureThreshold,
            final String healthy, final String unHealthy, final String thresholdLimit, final boolean canRunOnFailed) {
        super(threshold, newThreshold, failureThreshold, newFailureThreshold,
                healthy, unHealthy, thresholdLimit, canRunOnFailed, "FINDBUGS");
    }
    // CHECKSTYLE:ON

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    protected boolean acceptGoal(final String goal) {
        return "findbugs".equals(goal) || "site".equals(goal);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public ParserResult perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final PluginLogger logger) throws InterruptedException, IOException {
        List<String> sources = new ArrayList<String>(pom.getCompileSourceRoots());
        sources.addAll(pom.getTestCompileSourceRoots());

        FilesParser findBugsCollector = new FilesParser(logger, determineFileName(mojo),
                    new FindBugsParser(sources), getModuleName(pom));

        return getTargetPath(pom).act(findBugsCollector);
    }

    /** {@inheritDoc} */
    @Override
    protected BuildResult persistResult(final ParserResult project, final MavenBuild build) {
        FindBugsResult result = new FindBugsResult(build, getDefaultEncoding(), project);
        build.getActions().add(new MavenFindBugsResultAction(build, this, getDefaultEncoding(), result));
        build.registerAsProjectAction(FindBugsReporter.this);

        return result;
    }

    /**
     * Determines the filename of the FindBugs results.
     *
     * @param mojo the mojo containing the FindBugs configuration
     * @return filename of the FindBugs results
     */
    private String determineFileName(final MojoInfo mojo) {
        String fileName = FINDBUGS_XML_FILE;
        try {
            Boolean isNativeFormat = mojo.getConfigurationValue("findbugsXmlOutput", Boolean.class);
            if (Boolean.FALSE.equals(isNativeFormat)) {
                fileName = MAVEN_FINDBUGS_XML_FILE;
            }
        }
        catch (ComponentConfigurationException exception) {
            // ignore and assume new format
        }
        return fileName;
    }

    /** {@inheritDoc} */
    @Override
    public List<FindBugsProjectAction> getProjectActions(final MavenModule module) {
        return Collections.singletonList(new FindBugsProjectAction(module));
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends Action> getResultActionClass() {
        return MavenFindBugsResultAction.class;
    }

    /** Ant file-set pattern of files to work with. @deprecated */
    @SuppressWarnings("unused")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    @Deprecated
    private transient String pattern; // obsolete since release 2.5
}

