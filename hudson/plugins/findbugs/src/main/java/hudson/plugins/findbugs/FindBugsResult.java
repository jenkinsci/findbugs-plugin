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
    private final int numberOfWarnings;
    /** Difference between this and the previous build. */
    private final int delta;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param currentNumberOfWarnings
     *            current number of warnings
     * @param previousNumberOfWarnings
     *            previous number of warnings
     */
    public FindBugsResult(final int currentNumberOfWarnings, final int previousNumberOfWarnings) {
        numberOfWarnings = currentNumberOfWarnings;
        delta = currentNumberOfWarnings - previousNumberOfWarnings;
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
        return delta;
    }
}
