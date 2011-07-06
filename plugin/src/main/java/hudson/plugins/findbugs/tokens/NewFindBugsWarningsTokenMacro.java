package hudson.plugins.findbugs.tokens;

import hudson.Extension;
import hudson.plugins.analysis.tokens.AbstractNewAnnotationsTokenMacro;
import hudson.plugins.findbugs.FindBugsMavenResultAction;
import hudson.plugins.findbugs.FindBugsResultAction;

/**
 * Provides a token that evaluates to the number of new FindBugs warnings.
 *
 * @author Ulli Hafner
 */
@Extension(optional = true)
public class NewFindBugsWarningsTokenMacro extends AbstractNewAnnotationsTokenMacro {
    /**
     * Creates a new instance of {@link NewFindBugsWarningsTokenMacro}.
     */
    @SuppressWarnings("unchecked")
    public NewFindBugsWarningsTokenMacro() {
        super("FINDBUGS_NEW", FindBugsResultAction.class, FindBugsMavenResultAction.class);
    }
}

