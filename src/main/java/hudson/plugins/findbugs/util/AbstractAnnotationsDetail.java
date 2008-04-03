package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.util.model.AnnotationContainer;
import hudson.plugins.findbugs.util.model.AnnotationProvider;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Collection;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class for annotation detail objects. Instances of this class could be used for
 * Hudson Stapler objects that contain a subset of annotations.
 */
public abstract class AbstractAnnotationsDetail extends AnnotationContainer implements ModelObject {
    /** Current build as owner of this object. */
    private final AbstractBuild<?, ?> owner;

    /**
     * Creates a new instance of <code>AbstractWarningsDetail</code>.
     *
     * @param owner
     *            current build as owner of this object.
     * @param annotations
     *            the set of warnings represented by this object
     */
    public AbstractAnnotationsDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations) {
        super();
        this.owner = owner;

        addAnnotations(annotations);
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this build is the last available build.
     *
     * @return <code>true</code> if this build is the last available build
     */
    public final boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Creates a detail graph for the specified detail object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param detailObject
     *            the detail object to compute the graph for
     * @param upperBound
     *            the upper bound of all tasks
     * @throws IOException
     *             in case of an error
     */
    protected final void createDetailGraph(final StaplerRequest request, final StaplerResponse response,
            final AnnotationProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        JFreeChart chart = ChartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfAnnotations(Priority.HIGH),
                detailObject.getNumberOfAnnotations(Priority.NORMAL),
                detailObject.getNumberOfAnnotations(Priority.LOW), upperBound);
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }

    /**
     * Returns the package category name for the scanned files. Currently, only
     * java and c# files are supported.
     *
     * @return the package category name for the scanned files
     */
    public String getPackageCategoryName() {
        if (hasAnnotations()) {
            String fileName = getAnnotations().iterator().next().getFileName();
            if (fileName.endsWith(".cs")) {
                return hudson.plugins.findbugs.util.Messages.NamespaceDetail_header();
            }
        }
        return hudson.plugins.findbugs.util.Messages.PackageDetail_header();
    }

    /**
     * Returns a localized priority name.
     *
     * @param priorityName
     *            priority as String value
     * @return localized priority name
     */
    public String getLocalizedPriority(final String priorityName) {
        return Priority.fromString(priorityName).getLongLocalizedString();
    }
}
