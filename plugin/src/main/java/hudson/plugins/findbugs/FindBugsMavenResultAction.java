package hudson.plugins.findbugs;

import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.MavenResultAction;

import java.util.List;
import java.util.Map;

/**
 * A {@link FindBugsResultAction} for native Maven jobs. This action
 * additionally provides result aggregation for sub-modules and for the main
 * project.
 *
 * @author Ulli Hafner
 */
public class FindBugsMavenResultAction extends MavenResultAction<FindBugsResult> {
    /**
     * Creates a new instance of {@link FindBugsMavenResultAction}. This instance
     * will have no result set in the beginning. The result will be set
     * successively after each of the modules are build.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public FindBugsMavenResultAction(final MavenModuleSetBuild owner, final HealthDescriptor healthDescriptor,
            final String defaultEncoding) {
        super(new FindBugsResultAction(owner, healthDescriptor), defaultEncoding, "FINDBUGS");
    }

    /**
     * Creates a new instance of {@link FindBugsMavenResultAction}.
     *
     * @param owner
     *            the associated build of this action
     * @param healthDescriptor
     *            health descriptor to use
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the result in this build
     */
    public FindBugsMavenResultAction(final MavenBuild owner, final HealthDescriptor healthDescriptor,
            final String defaultEncoding, final FindBugsResult result) {
        super(new FindBugsResultAction(owner, healthDescriptor, result), defaultEncoding, "FINDBUGS");
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new FindBugsMavenResultAction(build, getHealthDescriptor(), getDisplayName());
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new FindBugsProjectAction(moduleSet);
    }

    @Override
    public Class<? extends MavenResultAction<FindBugsResult>> getIndividualActionType() {
        return FindBugsMavenResultAction.class;
    }

    @Override
    protected FindBugsResult createResult(final FindBugsResult... results) {
        return new FindBugsResult(getOwner(), results[0].getDefaultEncoding(), aggregate(results));
    }
}

