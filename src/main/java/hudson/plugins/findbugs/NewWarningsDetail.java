package hudson.plugins.findbugs;

import hudson.model.Build;

import java.util.Set;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
        return new FindBugsSource(getOwner(), getWarning(link));
    }
}

