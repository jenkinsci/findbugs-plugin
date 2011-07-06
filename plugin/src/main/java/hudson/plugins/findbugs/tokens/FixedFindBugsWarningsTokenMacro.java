package hudson.plugins.findbugs.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractFixedAnnotationsTokenMacro;
import hudson.plugins.findbugs.FindBugsMavenResultAction;
import hudson.plugins.findbugs.FindBugsResultAction;

/**
 * Provides a token that evaluates to the number of fixed FindBugs warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class FixedFindBugsWarningsTokenMacro extends AbstractFixedAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link FixedFindBugsWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public FixedFindBugsWarningsTokenMacro() {
        super("ANALYSIS_FIXED", FindBugsResultAction.class, FindBugsMavenResultAction.class);
    }
}

