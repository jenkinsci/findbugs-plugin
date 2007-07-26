package hudson.plugins.findbugs;

import hudson.model.Descriptor;
import hudson.tasks.Publisher;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Descriptor for the class {@link FindBugsPublisher}. Used as a singleton.
 * The class is marked as public so that it can be accessed from views.
 */
public final class FindBugsDescriptor extends Descriptor<Publisher> {
    /**
     * Instantiates a new find bugs descriptor.
     */
    FindBugsDescriptor() {
        super(FindBugsPublisher.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Publish Findbugs Analysis Results";
    }

    /** {@inheritDoc} */
    @Override
    public String getHelpFile() {
        return "/plugin/findbugs/help.html";
    }

    /** {@inheritDoc} */
    @Override
    public FindBugsPublisher newInstance(final StaplerRequest request) throws FormException {
        return request.bindParameters(FindBugsPublisher.class, "findbugs_");
    }
}