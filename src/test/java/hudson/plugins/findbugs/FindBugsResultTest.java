package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.test.BuildResultTest;

/**
 * Tests the class {@link FindBugsResult}.
 */
public class FindBugsResultTest extends BuildResultTest<FindBugsResult> {
    /** {@inheritDoc} */
    @Override
    protected FindBugsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final BuildHistory history) {
        return new FindBugsResult(build, null, project, history);
    }
}

