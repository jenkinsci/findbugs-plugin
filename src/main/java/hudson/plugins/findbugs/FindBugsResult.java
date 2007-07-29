package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
// FIXME: make the result non-transient and reload on request
public class FindBugsResult implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;
    /** Difference between this and the previous build. */
    private final int delta;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The parsed FindBugs result. */
    @SuppressWarnings("Se")
    private final JavaProject project;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project) {
        owner = build;
        this.project = project;
        FindBugsResultAction action = build.getAction(FindBugsResultAction.class);
        if (action.hasPreviousResult()) {
            delta = project.getNumberOfWarnings() - action.getPreviousResult().getResult().getNumberOfWarnings();
        }
        else {
            delta = 0;
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Package Statistics";
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
     * Returns the numberOfWarnings.
     *
     * @return the numberOfWarnings
     */
    public int getNumberOfWarnings() {
        return project.getNumberOfWarnings();
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        return project;
    }

    /**
     * Returns the number of warnings of the specified package in the previous build.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getPreviousNumberOfWarnings(final String packageName) {
        FindBugsResultAction action = owner.getAction(FindBugsResultAction.class);
        if (action.hasPreviousResult()) {
            return action.getPreviousResult().getResult().getProject().getNumberOfWarnings(packageName);
        }
        else {
            return 0;
        }
    }
}
