package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.JavaPackage;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the package statistics of a module.
 */
public class PackageDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** The package to show the details for. */
    private final JavaPackage javaPackage;
    /** Header in jelly script. */
    private final String header;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param javaPackage
     *            the package to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public PackageDetail(final AbstractBuild<?, ?> owner, final JavaPackage javaPackage, final String header) {
        super(owner, javaPackage.getAnnotations());
        this.javaPackage = javaPackage;
        this.header = header;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return javaPackage.getName();
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return header + " - " + Messages.PackageDetail_header(javaPackage.getName());
    }

    /**
     * Returns the maven module.
     *
     * @return the maven module
     */
    public JavaPackage getPackage() {
        return javaPackage;
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
        return new SourceDetail(getOwner(), getAnnotation(link));
    }
}

