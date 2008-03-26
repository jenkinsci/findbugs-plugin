package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractAnnotationsDetail;
import hudson.plugins.findbugs.util.model.FileAnnotation;

import java.util.Set;

/**
 * Result object to visualize the fixed warnings in a build.
 */
public class FixedWarningsDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -8601095040123486522L;

    /**
     * Creates a new instance of <code>FixedWarningsDetail</code>.
     *
     * @param owner
     *            the current results object as owner of this action
     * @param fixedWarnings
     *            all fixed warnings in this build
     */
    public FixedWarningsDetail(final AbstractBuild<?, ?> owner, final Set<FileAnnotation> fixedWarnings) {
        super(owner, fixedWarnings);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_FixedWarningsDetail_Name();
    }
}

