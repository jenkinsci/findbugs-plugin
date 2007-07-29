package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;

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
    private final Build<?, ?> owner;
    /** The parsed FindBugs result. */
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
        delta = 0;
        this.project = project;
    }

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     * @param previousProject
     *            the parsed FindBugs result of the previous session
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project, final JavaProject previousProject) {
        owner = build;
        delta = project.getNumberOfWarnings() - previousProject.getNumberOfWarnings();
        this.project = project;
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

    public JavaProject getProject() {
        return project;
    }
}
