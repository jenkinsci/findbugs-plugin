package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.model.AnnotationContainer;
import hudson.plugins.findbugs.model.AnnotationProvider;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.util.ChartBuilder;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.util.Collection;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class for warning detail objects.
 */
public abstract class AbstractWarningsDetail extends AnnotationContainer implements ModelObject {
    /** Current build as owner of this action. */
    private final AbstractBuild<?, ?> owner;

    /**
     * Creates a new instance of <code>AbstractWarningsDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param annotations
     *            the set of warnings represented by this object
     */
    public AbstractWarningsDetail(final AbstractBuild<?, ?> owner, final Collection<FileAnnotation> annotations) {
        super();
        this.owner = owner;

        addAnnotations(annotations);
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
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
}
