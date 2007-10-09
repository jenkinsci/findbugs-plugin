package hudson.plugins.findbugs;

import hudson.model.Build;

import java.io.IOException;
import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the package statistics of a module.
 */
public class ModuleDetail extends AbstractWarningsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1854984151887397361L;
    /** The module to show the details for. */
    private final Module module;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param module
     *            the module to show the details for
     */
    public ModuleDetail(final Build<?, ?> owner, final Module module) {
        super(owner, module.getWarnings());
        this.module = module;
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
    public Module getModule() {
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
        createDetailGraph(request, response, module.getPackage(request.getParameter("package")), module.getWarningBound());
    }

    /**
     * Returns the dynamic result of this FindBugs detail view. Depending on the
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
     * @return the dynamic result of the FindBugs analysis (detail page for a
     *         package).
     */
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        if (isSinglePackageModule()) {
            return new FindBugsSource(getOwner(), link);
        }
        else {
            return new PackageDetail(getOwner(), module.getPackage(link));
        }
    }
}

