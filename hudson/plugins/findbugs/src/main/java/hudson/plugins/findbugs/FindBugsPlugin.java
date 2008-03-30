package hudson.plugins.findbugs;

import hudson.Plugin;
import hudson.maven.MavenReporters;
import hudson.tasks.BuildStep;

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
        BuildStep.PUBLISHERS.addRecorder(FindBugsPublisher.FIND_BUGS_DESCRIPTOR);
        FindBugsMessages.getInstance().initialize();

        MavenReporters.LIST.add(FindBugsReporter.FINDBUGS_SCANNER_DESCRIPTOR);
    }
}
