package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.maven.MavenBuild;
import hudson.maven.MavenBuildProxy;
import hudson.maven.MavenModule;
import hudson.maven.MavenReporter;
import hudson.maven.MavenReporterDescriptor;
import hudson.maven.MojoInfo;
import hudson.maven.MavenBuildProxy.BuildCallable;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.findbugs.parser.FindBugsCollector;
import hudson.plugins.findbugs.parser.PlainFindBugsParser;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.TrendReportSize;
import hudson.plugins.findbugs.util.model.JavaProject;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.project.MavenProject;

import edu.umd.cs.findbugs.DetectorFactoryCollection;

// FIXME: this class more or less is a copy of the FindBugsPublisher, we should find a way to generalize portions of this class
public class FindBugsReporter extends MavenReporter {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -288391908253344862L;
    /** Descriptor of this publisher. */
    public static final FindBugsReporterDescriptor FINDBUGS_SCANNER_DESCRIPTOR = new FindBugsReporterDescriptor(FindBugsPublisher.FIND_BUGS_DESCRIPTOR);
    /** Default FindBugs pattern. */
    private static final String DEFAULT_PATTERN = "**/findbugsXml.xml";
    /** Ant file-set pattern of files to work with. */
    private final String pattern;
    /** Annotation threshold to be reached if a build should be considered as unstable. */
    private final String threshold;
    /** Determines whether to use the provided threshold to mark a build as unstable. */
    private boolean thresholdEnabled;
    /** Integer threshold to be reached if a build should be considered as unstable. */
    private int minimumAnnotations;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final String healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final String unHealthy;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthyAnnotations;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthyAnnotations;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean healthyReportEnabled;
    /** Determines the height of the trend graph. */
    private final String height;
    /** Lock to prevent several calls to FindBugs initializations. */
    private transient Boolean lockLibraryInitialization = Boolean.TRUE;
    /** Determines whether the FindBugs library has been initialized yet. */
    private transient boolean isInitialized;

    /**
     * Creates a new instance of <code>FindBugsReporter</code>.
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
     * @param height
     *            the height of the trend graph
     * @stapler-constructor
     */
    public FindBugsReporter(final String pattern, final String threshold, final String healthy, final String unHealthy, final String height) {
        super();
        this.threshold = threshold;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        this.pattern = pattern;
        this.height = height;

        if (!StringUtils.isEmpty(threshold)) {
            try {
                minimumAnnotations = Integer.valueOf(threshold);
                if (minimumAnnotations >= 0) {
                    thresholdEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
        if (!StringUtils.isEmpty(healthy) && !StringUtils.isEmpty(unHealthy)) {
            try {
                healthyAnnotations = Integer.valueOf(healthy);
                unHealthyAnnotations = Integer.valueOf(unHealthy);
                if (healthyAnnotations >= 0 && unHealthyAnnotations > healthyAnnotations) {
                    healthyReportEnabled = true;
                }
            }
            catch (NumberFormatException exception) {
                // nothing to do, we use the default value
            }
        }
    }

    /**
     * Initializes transient fields.
     *
     * @return the created object
     */
    private Object readResolve() {
        lockLibraryInitialization = Boolean.TRUE;
        isInitialized = false;
        return this;
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
     * Returns the Ant file-set pattern to the workspace files.
     *
     * @return ant file-set pattern to the workspace files.
     */
    public String getPattern() {
        return pattern;
    }

    /** {@inheritDoc} */
    @Override
    public boolean postExecute(final MavenBuildProxy build, final MavenProject pom, final MojoInfo mojo,
            final BuildListener listener, final Throwable error) throws InterruptedException, IOException {
        if (!"findbugs".equals(mojo.getGoal()) && !"site".equals(mojo.getGoal())) {
            return true;
        }
        if (hasResultAction(build)) {
            listener.getLogger().println("Scipping findbugs plug-in: there is already a result available.");
            return true;
        }

        synchronized (lockLibraryInitialization) {
            if (!isInitialized) {
                initializeFindBugsLibrary(build);
                isInitialized = true;
            }
        }

        FilePath pomPath = new FilePath(pom.getBasedir());
        FindBugsCollector findBugsCollector = new FindBugsCollector(listener.getLogger(),
                build.getTimestamp().getTimeInMillis(),
                StringUtils.defaultIfEmpty(getPattern(), DEFAULT_PATTERN), false);
        final JavaProject project = pomPath.act(findBugsCollector);

        build.execute(new BuildCallable<Void, IOException>() {
            public Void call(final MavenBuild build) throws IOException, InterruptedException {
                FindBugsResult result = new FindBugsResultBuilder().build(build, project);
                HealthReportBuilder healthReportBuilder = new HealthReportBuilder(thresholdEnabled, minimumAnnotations,
                        healthyReportEnabled, healthyAnnotations, unHealthyAnnotations,
                        Messages.FindBugs_ResultAction_HealthReportSingleItem(),
                        Messages.FindBugs_ResultAction_HealthReportMultipleItem("%d"));
                build.getActions().add(new MavenFindBugsResultAction(build, healthReportBuilder, height, result));
                build.registerAsProjectAction(FindBugsReporter.this);

                return null;
            }
        });

        int warnings = project.getNumberOfAnnotations();
        if (warnings > 0) {
            listener.getLogger().println("A total of " + warnings + " potential bugs have been found.");
            if (thresholdEnabled && warnings >= minimumAnnotations) {
                build.setResult(Result.UNSTABLE);
            }
        }
        else {
            listener.getLogger().println("No potential bugs have been found.");
        }

        return true;
    }

    /**
     * Returns whether we already have a result for this build.
     *
     * @param build
     *            the current build.
     * @return <code>true</code> if we already have a task result action.
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if the call has been interrupted
     */
    private Boolean hasResultAction(final MavenBuildProxy build) throws IOException, InterruptedException {
        return build.execute(new BuildCallable<Boolean, IOException>() {
            public Boolean call(final MavenBuild mavenBuild) throws IOException, InterruptedException {
                return mavenBuild.getAction(MavenFindBugsResultAction.class) != null;
            }
        });
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
    private void initializeFindBugsLibrary(final MavenBuildProxy build) throws IOException,
            InterruptedException {
        URL[] urls = build.execute(new BuildCallable<URL[], IOException>() {
            public URL[] call(final MavenBuild build) throws IOException, InterruptedException {
                return PlainFindBugsParser.createPluginUrls();
            }
        });
        DetectorFactoryCollection.rawInstance().setPluginList(urls);
    }

    /** {@inheritDoc} */
    @Override
    public Action getProjectAction(final MavenModule module) {
        return new FindBugsProjectAction(module, getTrendHeight());
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporterDescriptor getDescriptor() {
        return FINDBUGS_SCANNER_DESCRIPTOR;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public String getHeight() {
        return height;
    }

    /**
     * Returns the height of the trend graph.
     *
     * @return the height of the trend graph
     */
    public int getTrendHeight() {
        return new TrendReportSize(height).getHeight();
    }
}

