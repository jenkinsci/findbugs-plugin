package hudson.plugins.findbugs;

import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.model.Action;
import hudson.plugins.findbugs.parser.FindBugsCollector;
import hudson.plugins.findbugs.parser.PlainFindBugsParser;
import hudson.plugins.findbugs.util.HealthAwareMavenReporter;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.model.JavaProject;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.DetectorFactoryCollection;

/**
 * Publishes the results of the FindBugs analysis (maven 2 project type).
 *
 * @author Ulli Hafner
 */
public class FindBugsReporter extends HealthAwareMavenReporter {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -288391908253344862L;
    /** Descriptor of this publisher. */
    public static final FindBugsReporterDescriptor FINDBUGS_SCANNER_DESCRIPTOR = new FindBugsReporterDescriptor(FindBugsPublisher.FIND_BUGS_DESCRIPTOR);
    /** FindBugs filename if maven findbugsXmlOutput is activated. */
    private static final String FINDBUGS_XML_FILE = "findbugsXml.xml";
    /** FindBugs filename if maven findbugsXmlOutput is not activated. */
    private static final String MAVEN_FINDBUGS_XML_FILE = "findbugs.xml";
    /** Ant file-set pattern of files to work with. */
    @SuppressWarnings("unused")
    private String pattern; // obsolete since release 2.5
    /** Lock to prevent several calls to FindBugs initializations. */
    private transient Object lockLibraryInitialization = new Object();
    /** Determines whether the FindBugs library has been initialized yet. */
    private transient boolean isInitialized;

    /**
     * Creates a new instance of <code>FindBugsReporter</code>.
     *
     * @param threshold
     *            Bug threshold to be reached if a build should be considered as
     *            unstable.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     * @param height
     *            the height of the trend graph
     */
    @DataBoundConstructor
    public FindBugsReporter(final String threshold, final String healthy, final String unHealthy, final String height) {
        super(threshold, healthy, unHealthy, height, "FINDBUGS");
    }

    /**
     * Initializes transient fields.
     *
     * @return the created object
     */
    private Object readResolve() {
        lockLibraryInitialization = new Object();
        isInitialized = false;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean acceptGoal(final String goal) {
        return "findbugs".equals(goal) || "site".equals(goal);
    }

    /** {@inheritDoc} */
    @Override
    public JavaProject perform(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo, final PrintStream logger) throws InterruptedException, IOException {
        synchronized (lockLibraryInitialization) {
            if (!isInitialized) {
                initializeFindBugsLibrary(build);
                isInitialized = true;
            }
        }
        FindBugsCollector findBugsCollector = new FindBugsCollector(logger, determineFileName(mojo), false);

        return getTargetPath(pom).act(findBugsCollector);
    }

    /** {@inheritDoc} */
    @Override
    protected void persistResult(final JavaProject project, final MavenBuild build) {
        FindBugsResult result = new FindBugsResultBuilder().build(build, project);
        HealthReportBuilder healthReportBuilder = createHealthBuilder(
                Messages.FindBugs_ResultAction_HealthReportSingleItem(),
                Messages.FindBugs_ResultAction_HealthReportMultipleItem("%d"));
        build.getActions().add(new MavenFindBugsResultAction(build, healthReportBuilder, getHeight(), result));
        build.registerAsProjectAction(FindBugsReporter.this);
    }

    /**
     * Determines the filename of the FindBugs results.
     *
     * @param mojo the mojo containing the FindBugs configuration
     * @return filename of the FindBugs results
     */
    private String determineFileName(final MojoInfo mojo) {
        String fileName = MAVEN_FINDBUGS_XML_FILE;
        try {
            Boolean isNativeFormat = mojo.getConfigurationValue("findbugsXmlOutput", Boolean.class);
            if (Boolean.TRUE.equals(isNativeFormat)) {
                fileName = FINDBUGS_XML_FILE;
            }
        }
        catch (ComponentConfigurationException exception) {
            // ignore and use old format
        }
        return fileName;
    }

    /**
     * Initializes the native FindBugs library.
     *
     * @param build
     *            the current build
     * @throws IOException
     *             in case of an error
     * @throws InterruptedException
     *             in case of an error
     */
    // TODO: this hack works only if slave and master are on the same machine
    private void initializeFindBugsLibrary(final MavenBuildProxy build) throws IOException, InterruptedException {
        DetectorFactoryCollection.rawInstance().setPluginList(build.execute(new FindBugsLibraryUrlDetector()));
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final MavenModule module) {
        return new FindBugsProjectAction(module, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends Action> getResultActionClass() {
        return MavenFindBugsResultAction.class;
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return FINDBUGS_SCANNER_DESCRIPTOR;
    }

    /**
     * Detects the URLs of the find-bugs library plug-ins.
     */
    private static final class FindBugsLibraryUrlDetector implements BuildCallable<URL[], IOException> {
        /** Unique identifier of this class. */
        private static final long serialVersionUID = 8714527548935228652L;

        /** {@inheritDoc} */
        public URL[] call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
            return PlainFindBugsParser.createPluginUrls();
        }
    }
}

