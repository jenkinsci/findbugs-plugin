package hudson.plugins.findbugs.util;

import hudson.model.Build;
import hudson.util.ChartUtil;

import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Controls the live cycle of Hudson results. This action persists the results
 * of a build and displays them on the build page. The actual visualization of
 * the results is defined in the matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the results trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public abstract class AbstractResultAction {
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private Build<?, ?> owner;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     */
    public AbstractResultAction() {
        // nothing to do. used to deserialize this action
    }

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     */
    public AbstractResultAction(final Build<?, ?> owner) {
        super();
        this.owner = owner;
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Generates a PNG image for the result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(request, response), WIDTH, HEIGHT);
    }

    /**
     * Generates a PNG image for the result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public final void doGraphMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        ChartUtil.generateClickableMap(request, response, createChart(request, response), WIDTH, HEIGHT);

    }

    /**
     * Creates the chart for this action.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the chart for this action.
     */
    protected abstract JFreeChart createChart(StaplerRequest request, StaplerResponse response);
}
