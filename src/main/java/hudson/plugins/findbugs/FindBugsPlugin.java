package hudson.plugins.findbugs;

import hudson.Plugin;

/**
 * Registers the FindBugs plug-in publisher.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
    public void start() throws Exception {
        FindBugsMessages.getInstance().initialize();
    }
}
