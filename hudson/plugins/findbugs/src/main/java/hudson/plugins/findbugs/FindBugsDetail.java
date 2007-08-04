package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Warning details of a package.
 */
public class FindBugsDetail implements ModelObject, Serializable  {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -2313908474981276271L;
    /** Package name of the details.*/
    private final String packageName;
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
     * @param packageName
     *            Package name of this details view.
     */
    public FindBugsDetail(final Build<?, ?> build, final JavaProject project, final String packageName) {
        owner = build;
        this.project = project;
        this.packageName = packageName;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Details";
    }

    /**
     * Returns the packageName.
     *
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns all the warnings in this package.
     *
     * @return all warnings of this package
     */
    public Collection<Warning> getWarnings() {
        return project.getWarnings(packageName);
    }

    /**
     * Returns the build.
     *
     * @return the build
     */
    public Build<?, ?> getOwner() {
        return owner;
    }
}

