package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Details";
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param link the package name to get the result for
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new FindBugsSource(owner, link);
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
     * Returns all warnings for this package details view. The returned collection is read-only.
     *
     * @return the warnings for this package details view
     */
    public Set<Warning> getWarnings() {
        return Collections.unmodifiableSet(project.getWarnings(packageName));
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

