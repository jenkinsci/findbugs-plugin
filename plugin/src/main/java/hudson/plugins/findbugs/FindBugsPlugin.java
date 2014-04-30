package hudson.plugins.findbugs;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecution;

import hudson.Plugin;

import hudson.plugins.analysis.core.PluginDescriptor;
import hudson.plugins.analysis.views.DetailFactory;

/**
 * Initializes the FindBugs messages, descriptions and detail view factory.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    @Override
    public void start() {
        initializeDetails();
    }

    private void initializeDetails() {
        FindBugsDetailFactory detailBuilder = new FindBugsDetailFactory();
        DetailFactory.addDetailBuilder(FindBugsResultAction.class, detailBuilder);
        if (PluginDescriptor.isMavenPluginInstalled()) {
            MavenInitialization.run(detailBuilder);
        }
    }

    /**
     * Returns whether the specified maven findbugs plug-in uses a FindBugs
     * release 2.0.0 or newer.
     *
     * @param mojoExecution
     *            the maven version ID
     * @return <code>true</code> if FindBugs 2.0.0 or newer is used
     */
    public static boolean isFindBugs2x(final MojoExecution mojoExecution) {
        try {
            String[] versions = StringUtils.split(mojoExecution.getVersion(), ".");
            if (versions.length > 1) {
                int major = Integer.parseInt(versions[0]);
                int minor = Integer.parseInt(versions[1]);
                return major > 2 || (major == 2 && minor >= 4);
            }
        }
        catch (Throwable exception) { // NOCHECKSTYLE NOPMD
            // ignore and return false
        }
        return false;
    }
}
