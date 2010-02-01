package hudson.plugins.findbugs;

import hudson.Plugin;

/**
 * Initializes the FindBugs messages and descriptions.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    public void start() throws Exception {
        FindBugsMessages.getInstance().initialize();
    }
}
