package hudson.plugins.findbugs;

import hudson.plugins.findbugs.util.PluginDescriptor;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link FindBugsPublisher}. Used as a singleton. The
 * class is marked as public so that it can be accessed from views.
 *
 * @author Ulli Hafner
 */
public final class FindBugsDescriptor extends PluginDescriptor {
    /** Plug-in name. */
    static final String PLUGIN_NAME = "findbugs";
    /** Icon to use for the result and project action. */
    static final String ACTION_ICON = "/plugin/findbugs/icons/findbugs-32x32.gif";

    /**
     * Instantiates a new find bugs descriptor.
     */
    FindBugsDescriptor() {
        super(FindBugsPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return Messages.FindBugs_Publisher_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return PLUGIN_NAME;
    }


    /** {@inheritDoc} */
    @Override
    public FindBugsPublisher newInstance(final StaplerRequest request) throws FormException {
        return request.bindParameters(FindBugsPublisher.class, PLUGIN_NAME + "_");
    }
}