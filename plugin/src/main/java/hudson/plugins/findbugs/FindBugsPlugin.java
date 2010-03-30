package hudson.plugins.findbugs;

import hudson.Plugin;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * Initializes the FindBugs messages and descriptions.
 *
 * @author Ulli Hafner
 */
public class FindBugsPlugin extends Plugin {
    /** {@inheritDoc} */
    @Override
    public void start() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();
    }
}
