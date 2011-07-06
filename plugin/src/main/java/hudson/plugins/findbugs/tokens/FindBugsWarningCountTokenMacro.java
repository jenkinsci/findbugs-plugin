package hudson.plugins.findbugs.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractAnnotationsCountTokenMacro;
import hudson.plugins.findbugs.FindBugsMavenResultAction;
import hudson.plugins.findbugs.FindBugsResultAction;

/**
 * Provides a token that evaluates to the number of FindBugs warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class FindBugsWarningCountTokenMacro extends AbstractAnnotationsCountTokenMacro {
    /**
     * Creates a new instance of {@link FindBugsWarningCountTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public FindBugsWarningCountTokenMacro() {
        super("FINDBUGS_COUNT", FindBugsResultAction.class, FindBugsMavenResultAction.class);
    }
}

