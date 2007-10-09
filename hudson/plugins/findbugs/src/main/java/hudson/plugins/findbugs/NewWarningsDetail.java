package hudson.plugins.findbugs;

import hudson.model.Build;

import java.util.Set;

/**
 * Result object to visualize the new warnings in a build.
 */
public class NewWarningsDetail extends AbstractWarningsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5093487322493056475L;

    /**
     * Creates a new instance of <code>FixedWarningsDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param newWarnings
     *            all new warnings in this build
     */
    public NewWarningsDetail(final Build<?, ?> owner, final Set<Warning> newWarnings) {
        super(owner, newWarnings);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "New Warnings";
    }
}

