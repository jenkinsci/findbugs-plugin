package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractAnnotationsBuildResultTest;
import hudson.plugins.findbugs.util.ParserResult;

/**
 * Tests the class {@link FindBugsResult}.
 */
public class FindBugsResultTest extends AbstractAnnotationsBuildResultTest<FindBugsResult> {
    /** {@inheritDoc} */
    @Override
    protected FindBugsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project) {
        return new FindBugsResult(build, project);
    }

    /** {@inheritDoc} */
    @Override
    protected FindBugsResult createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final FindBugsResult previous) {
        return new FindBugsResult(build, project, previous);
    }
}

