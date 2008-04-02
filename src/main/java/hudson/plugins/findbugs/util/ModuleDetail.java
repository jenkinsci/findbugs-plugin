package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.JavaPackage;
import hudson.plugins.findbugs.util.model.MavenModule;

import java.io.IOException;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the package statistics of a module.
 */
public class ModuleDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** The module to show the details for. */
    private final MavenModule module;
    /** Header in jelly script. */
    private final String header;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param module
     *            the module to show the details for
     * @param header
     *            header to be shown on detail page
     */
    public ModuleDetail(final AbstractBuild<?, ?> owner, final MavenModule module, final String header) {
        super(owner, module.getAnnotations());
        this.module = module;
        this.header = header;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return header + " - " + Messages.ModuleDetail_header(module.getName());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return module.getName();
    }

    /**
     * Returns the maven module.
     *
     * @return the maven module
     */
    public MavenModule getModule() {
        return module;
    }

    /**
     * Returns the packages of this module.
     *
     * @return the packages of this module
     */
    public Collection<JavaPackage> getPackages() {
        return module.getPackages();
    }

    /**
     * Returns whether this module contains just one Java package. In this case
     * we show the warnings statistics instead of package statistics.
     *
     * @return <code>true</code> if this project contains just one Java
     *         package
     */
    public boolean isSinglePackageModule() {
        return getPackages().size() == 1;
    }

    /**
     * Generates a PNG image for high/normal/low distribution of a Java package.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doPackageStatistics(final StaplerRequest request, final StaplerResponse response) throws IOException {
        createDetailGraph(request, response, module.getPackage(request.getParameter("package")), module.getAnnotationBound());
    }

    /**
     * Returns the dynamic result of this module detail view. Depending on the
     * number of packages, one of the following detail objects is returned:
     * <ul>
     * <li>A detail object for a single workspace file (if the module contains
     * only one package).</li>
     * <li>A package detail object for a specified package (in any other case).</li>
     * </ul>
     *
     * @param link
     *            the link to identify the sub page to show
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of this module detail view
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if (isSinglePackageModule()) {
            return new SourceDetail(getOwner(), getAnnotation(link));
        }
        else {
            return new PackageDetail(getOwner(), module.getPackage(link), header);
        }
    }

    /**
     * Returns a tooltip showing the distribution of priorities for the selected
     * package.
     *
     * @param packageName
     *            the package to show the distribution for
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip(final String packageName) {
        return module.getPackage(packageName).getToolTip();
    }
}

