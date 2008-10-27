package hudson.plugins.findbugs;

import static junit.framework.Assert.*;
import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractAnnotationsBuildResultTest;
import hudson.plugins.findbugs.util.AnnotationsBuildResult;
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

    /** {@inheritDoc} */
    @Override
    protected void verifyHighScoreMessage(final int expectedZeroWarningsBuildNumber, final boolean expectedIsNewHighScore, final long expectedHighScore, final long gap, final FindBugsResult result) {
        if (result.hasNoAnnotations() && result.getDelta() == 0) {
            assertTrue(result.getDetails().contains(Messages.FindBugs_ResultAction_NoWarningsSince(expectedZeroWarningsBuildNumber)));
            if (expectedIsNewHighScore) {
                long days = AnnotationsBuildResult.getDays(expectedHighScore);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.FindBugs_ResultAction_OneHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.FindBugs_ResultAction_MultipleHighScore(days)));
                }
            }
            else {
                long days = AnnotationsBuildResult.getDays(gap);
                if (days == 1) {
                    assertTrue(result.getDetails().contains(Messages.FindBugs_ResultAction_OneNoHighScore()));
                }
                else {
                    assertTrue(result.getDetails().contains(Messages.FindBugs_ResultAction_MultipleNoHighScore(days)));
                }
            }
        }
    }
}

