package hudson.plugins.findbugs;

import hudson.Plugin;
import hudson.plugins.analysis.views.DetailFactory;

import java.io.IOException;

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
}
