package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.BuildResult;

/**
 * Represents the aggregated results of the FindBugs analysis in m2 jobs.
 *
 * @author Ulli Hafner
 * @deprecated not used anymore
 */
@Deprecated
public class FindBugsMavenResult extends FindBugsResult {
    private static final long serialVersionUID = -4913938782537266259L;

    /**
     * Creates a new instance of {@link FindBugsMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    @SuppressWarnings("deprecation")
    public FindBugsMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result) {
        super(build, defaultEncoding, result, false, MavenFindBugsResultAction.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return MavenFindBugsResultAction.class;
    }
}

