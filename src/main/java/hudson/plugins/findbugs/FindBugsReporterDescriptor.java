package hudson.plugins.findbugs;

import hudson.maven.MavenReporter;
import hudson.plugins.findbugs.util.PluginDescriptor;
import hudson.plugins.findbugs.util.ReporterDescriptor;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;


/**
 * Descriptor for the class {@link FindBugsReporter}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public class FindBugsReporterDescriptor extends ReporterDescriptor {
    /**
     * Creates a new instance of <code>FindBugsReporterDescriptor</code>.
     *
     * @param pluginDescriptor
     *            the plug-in descriptor of the publisher
     */
    public FindBugsReporterDescriptor(final PluginDescriptor pluginDescriptor) {
        super(FindBugsReporter.class, pluginDescriptor);
    }

    /** {@inheritDoc} */
    @Override
    public MavenReporter newInstance(final StaplerRequest request, final JSONObject formData) throws FormException {
        return request.bindJSON(FindBugsReporter.class, formData);
    }
}
