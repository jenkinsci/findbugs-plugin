/*
 *
 */
package hudson.plugins.findbugs;

import hudson.maven.MavenAggregatedReport;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.HealthDescriptor;
import hudson.plugins.analysis.core.MavenResultAction;
import hudson.plugins.analysis.core.ParserResult;

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
    public FindBugsMavenResultAction(final AbstractBuild<?, ?> owner, final HealthDescriptor healthDescriptor,
            final String defaultEncoding, final FindBugsResult result) {
        super(new FindBugsResultAction(owner, healthDescriptor, result), defaultEncoding, "FINDBUGS");
    }

    /** {@inheritDoc} */
    public MavenAggregatedReport createAggregatedAction(final MavenModuleSetBuild build, final Map<MavenModule, List<MavenBuild>> moduleBuilds) {
        return new FindBugsMavenResultAction(build, getHealthDescriptor(), getDisplayName(),
                new FindBugsResult(build, getDefaultEncoding(), new ParserResult(), false));
    }

    /** {@inheritDoc} */
    public Action getProjectAction(final MavenModuleSet moduleSet) {
        return new FindBugsProjectAction(moduleSet, FindBugsMavenResultAction.class);
    }

    @Override
    public Class<? extends MavenResultAction<FindBugsResult>> getIndividualActionType() {
        return FindBugsMavenResultAction.class;
    }

    @Override
    protected FindBugsResult createResult(final FindBugsResult existingResult, final FindBugsResult additionalResult) {
        return new FindBugsReporterResult(getOwner(), additionalResult.getDefaultEncoding(),
                aggregate(existingResult, additionalResult), existingResult.useOnlyStableBuildsAsReference());
    }
}

