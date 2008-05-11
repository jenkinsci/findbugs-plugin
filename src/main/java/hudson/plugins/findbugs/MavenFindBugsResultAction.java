package hudson.plugins.findbugs;

import hudson.maven.AggregatableAction;
import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.TrendReportSize;
import hudson.plugins.findbugs.util.model.JavaProject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A {@link FindBugsResultAction} for native maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 */
public class MavenFindBugsResultAction extends FindBugsResultAction implements AggregatableAction, MavenAggregatedReport {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 1273798369273225973L;
    /** Determines the height of the trend graph. */
    private final String height;

    /**
     * Creates a new instance of <code>MavenFindBugsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param height
     *            the height of the trend graph
     */
    public MavenFindBugsResultAction(final MavenModuleSetBuild owner, final HealthReportBuilder healthReportBuilder, final String height) {
        super(owner, healthReportBuilder);
        this.height = height;
    }

    /**
     * Creates a new instance of <code>MavenFindBugsResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param height
     *            the height of the trend graph
     * @param result
     *            the result in this build
     */
    public MavenFindBugsResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder, final String height, final FindBugsResult result) {
        super(owner, healthReportBuilder, result);
        this.height = height;
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new MavenFindBugsResultAction(build, getHealthReportBuilder(), height);
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new FindBugsProjectAction(moduleSet, new TrendReportSize(height).getHeight());
    }

    /** {@inheritDoc} */
    public Class<? extends AggregatableAction> getIndividualActionType() {
        return getClass();
    }

    /**
     * Called whenever a new module build is completed, to update the
     * aggregated report. When multiple builds complete simultaneously,
     * Hudson serializes the execution of this method, so this method
     * needs not be concurrency-safe.
     *
     * @param moduleBuilds
     *      Same as <tt>MavenModuleSet.getModuleBuilds()</tt> but provided for convenience and efficiency.
     * @param newBuild
     *      Newly completed build.
     */
    public void update(final Map<MavenModule, List<MavenBuild>> moduleBuilds, final MavenBuild newBuild) {
        JavaProject project = new JavaProject();
        for (List<MavenBuild> builds : moduleBuilds.values()) {
            if (!builds.isEmpty()) {
                addModule(project, builds);
            }
        }
        setResult(new FindBugsResultBuilder().build(getOwner(), project));
    }

    /**
     * Adds a new module to the specified project. The new module is obtained
     * from the specified list of builds.
     *
     * @param project
     *            the project to add the module to
     * @param builds
     *            the builds for a module
     */
    private void addModule(final JavaProject project, final List<MavenBuild> builds) {
        MavenBuild mavenBuild = builds.get(0);
        MavenFindBugsResultAction action = mavenBuild.getAction(getClass());
        if (action != null) {
            Collection<hudson.plugins.findbugs.util.model.MavenModule> modules = action.getResult().getProject().getModules();
            for (hudson.plugins.findbugs.util.model.MavenModule mavenModule : modules) {
                project.addModule(mavenModule);
            }
        }
    }
}

