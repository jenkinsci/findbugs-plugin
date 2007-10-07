package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Base class for warning detail objects.
 */
public abstract class AbstractWarningsDetail implements ModelObject, Serializable {
    /** Current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** All fixed warnings in this build. */
    private final Set<Warning> warnings;

    /**
     * Creates a new instance of <code>AbstractWarningsDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param warnings
     *            the set of warnings represented by this object
     */
    public AbstractWarningsDetail(final Build<?, ?> owner, final Set<Warning> warnings) {
        this.owner = owner;
        this.warnings = warnings;
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Returns the set of warnings.
     *
     * @return the set of warnings
     */
    public final Set<Warning> getWarnings() {
        return warnings;
    }
}
