package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Result object to visualize the fixed warnings in a build.
 */
public class FixedWarningsDetail implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -8601095040123486522L;
    /** Current results object as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** All fixed warnings in this build. */
    private final Set<Warning> fixedWarnings;

    /**
     * Creates a new instance of <code>FixedWarningsDetail</code>.
     *
     * @param owner
     *            the current results object as owner of this action
     * @param fixedWarnings
     *            all fixed warnings in this build
     */
    public FixedWarningsDetail(final Build<?, ?> owner, final Set<Warning> fixedWarnings) {
        this.owner = owner;
        this.fixedWarnings = fixedWarnings;

    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "Fixed Warnings";
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the fixedWarnings.
     *
     * @return the fixedWarnings
     */
    public Set<Warning> getFixedWarnings() {
        return fixedWarnings;
    }
}

