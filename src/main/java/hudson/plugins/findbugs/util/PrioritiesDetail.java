package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.Priority;

import java.util.Collection;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Result object to visualize the priorities statistics of a module.
 */
public class PrioritiesDetail extends AbstractAnnotationsDetail {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5315146140343619856L;
    /** Priority of the annotations. */
    private final Priority priority;

    /**
     * Creates a new instance of <code>ModuleDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the package to show the details for
     * @param priority
     *            the priority of all annotations
     * @param header
     *            header to be shown on detail page
     */
    public PrioritiesDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations, final Priority priority, final String header) {
        super(owner, annotations, header);
        this.priority = priority;
    }

    /**
     * Returns the header for the detail screen.
     *
     * @return the header
     */
    public String getHeader() {
        return getTitle() + " - " + priority.getLongLocalizedString();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return priority.getLongLocalizedString();
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
    @Override
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new SourceDetail(getOwner(), getAnnotation(link));
    }
}

