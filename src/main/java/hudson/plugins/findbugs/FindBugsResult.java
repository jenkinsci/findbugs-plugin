package hudson.plugins.findbugs;

import hudson.model.ModelObject;

import java.io.Serializable;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 */
public class FindBugsResult implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;
    /** Total number of FindBugs warnings. */
    private int numberOfWarnings;
    /** Determines if there exists a delta to a previous build. */
    private boolean isDeltaValid;
    /** Difference between this ands the previous build. */
    private int delta;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param numberOfWarnings
     *            the number of FindBugs warnings
     */
    public FindBugsResult(final int numberOfWarnings) {
        this.numberOfWarnings = numberOfWarnings;
    }

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     * @param currentNumberOfWarnings current number of warnings
     * @param previousNumberOfWarnings previous number of warnings
     */
    public FindBugsResult(final int currentNumberOfWarnings, final int previousNumberOfWarnings) {
        this(currentNumberOfWarnings);
        if (previousNumberOfWarnings != currentNumberOfWarnings) {
            delta = currentNumberOfWarnings - previousNumberOfWarnings;
            isDeltaValid = true;
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "TODO: Where is this text shown";
    }

    /**
     * Returns the numberOfWarnings.
     *
     * @return the numberOfWarnings
     */
    public int getNumberOfWarnings() {
        return numberOfWarnings;
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        if (!isDeltaValid) {
            throw new IllegalStateException("There is no delta defined for this build");
        }
        return delta;
    }

    /**
     * Returns if there exists a delta to a previous build.
     *
     * @return <code>true</code> if there exists a delta to a previous build
     */
    public boolean hasDelta() {
        return isDeltaValid;
    }

    /**
     * Sets the numberOfWarnings to the specified value.
     *
     * @param numberOfWarnings the value to set
     */
    public void setNumberOfWarnings(final int numberOfWarnings) {
        this.numberOfWarnings = numberOfWarnings;
    }
}
