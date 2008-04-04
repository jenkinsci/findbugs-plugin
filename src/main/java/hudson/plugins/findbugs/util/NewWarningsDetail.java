package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.FileAnnotation;

import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the new warnings in a build.
 */
public class NewWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5093487322493056475L;

    /**
     * Creates a new instance of <code>NewWarningsDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param newWarnings
     *            all new warnings in this build
     * @param header
     *            header to be shown on detail page
     */
    public NewWarningsDetail(final AbstractBuild<?, ?> owner, final Set<FileAnnotation> newWarnings, final String header) {
        super(owner, newWarnings, header);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.NewWarningsDetail_Name();
    }

    /**
     * Returns the dynamic result of this object (detail page for a source file).
     *
     * @param link the source file to get the result for
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of this object (detail page for a source file).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new SourceDetail(getOwner(), getAnnotation(link));
    }
}

