package hudson.plugins.findbugs;

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Initializes the FindBugs messages, descriptions and detail view factory.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    public void start() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();
        FindBugsDetailFactory detailBuilder = new FindBugsDetailFactory();
        DetailFactory.addDetailBuilder(FindBugsResultAction.class, detailBuilder);
        DetailFactory.addDetailBuilder(FindBugsMavenResultAction.class, detailBuilder);
    }

    /**
     * Returns whether the specified maven findbugs plug-in uses a FindBugs
     * release 2.0.0 or newer.
     *
     * @param version
     *            the maven version ID
     * @return <code>true</code> if FindBugs 2.0.0 or newer is used
     */
    public static boolean isFindBugs2x(final String version) {
        try {
            String[] versions = StringUtils.split(version, ".");
            if (versions.length > 1) {
                int major = Integer.parseInt(versions[0]);
                int minor = Integer.parseInt(versions[1]);
                return major > 2 || (major == 2 && minor >= 4);
            }
        }
        catch (NumberFormatException exception) {
            // ignore and return false
        }
        return false;
    }
}
