package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;

/**
 * Represents the aggregated results of the FindBugs analysis in m2 jobs.
 *
 * @author Ulli Hafner
 */
public class FindBugsReporterResult extends FindBugsResult {
    private static final long serialVersionUID = -1964303936149388262L;

    /**
     * Creates a new instance of {@link FindBugsReporterResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param usePreviousBuildAsReference
     *            determines whether to use the previous build as the reference
     *            build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     */
    public FindBugsReporterResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        super(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference,
                FindBugsMavenResultAction.class);
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return FindBugsMavenResultAction.class;
    }
}

