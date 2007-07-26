package hudson.plugins.findbugs;

import hudson.Plugin;
import hudson.tasks.BuildStep;

/**
 * Registers the findbugs plug-in publisher.
 *
 * @author Ulli Hafner
 * @plugin
 */
public class FindBugsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
    public void start() throws Exception {
        BuildStep.PUBLISHERS.add(FindBugsPublisher.FIND_BUGS_DESCRIPTOR);
    }
}
