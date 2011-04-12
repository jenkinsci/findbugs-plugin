package hudson.plugins.findbugs.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractResultTokenMacro;
import hudson.plugins.findbugs.FindBugsResultAction;

/**
 * Provides a token that evaluates to the number of FindBugs warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class FindBugsWarningCountTokenMacro extends AbstractResultTokenMacro {
    /**
     * Creates a new instance of {@link FindBugsWarningCountTokenMacro}.
     */
    public FindBugsWarningCountTokenMacro() {
        super(FindBugsResultAction.class, "FINDBUGS_COUNT");
    }
}

