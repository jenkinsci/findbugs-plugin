package hudson.plugins.findbugs;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecution;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;

import hudson.Plugin;

import hudson.plugins.analysis.views.DetailFactory;

/**
 * Initializes the FindBugs messages, descriptions and detail view factory.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    /** Property of SAX parser factory. */
    static final String SAX_DRIVER_PROPERTY = "org.xml.sax.driver";

    @Override
    public void start() throws IOException, SAXException {
        String oldProperty = System.getProperty(SAX_DRIVER_PROPERTY);
        System.setProperty(SAX_DRIVER_PROPERTY, SAXParser.class.getName());

        FindBugsMessages.getInstance().initialize();
        FindBugsDetailFactory detailBuilder = new FindBugsDetailFactory();
        DetailFactory.addDetailBuilder(FindBugsResultAction.class, detailBuilder);
        DetailFactory.addDetailBuilder(FindBugsMavenResultAction.class, detailBuilder);

        if (oldProperty != null) {
            System.setProperty(SAX_DRIVER_PROPERTY, oldProperty);
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
