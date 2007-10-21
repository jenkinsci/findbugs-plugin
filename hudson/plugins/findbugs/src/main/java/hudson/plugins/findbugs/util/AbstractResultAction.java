package hudson.plugins.findbugs.util;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.util.ChartUtil;

import java.io.IOException;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerProxy;
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
public abstract class AbstractResultAction implements StaplerProxy, HealthReportingAction {
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private Build<?, ?> owner;
    /** Builds a health report. */
    private HealthReportBuilder healthReportBuilder;

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
     * @param healthReportBuilder
     *            health builder to use
     */
    public AbstractResultAction(final Build<?, ?> owner, final HealthReportBuilder healthReportBuilder) {
        super();
        this.owner = owner;
        this.healthReportBuilder = healthReportBuilder;
    }

    /**
     * Returns the associated health report builder.
     *
     * @return the associated health report builder
     */
    public final HealthReportBuilder getHealthReportBuilder() {
        if (healthReportBuilder == null) { // support for old serialization information
            healthReportBuilder = new HealthReportBuilder();
        }
        return healthReportBuilder;
    }

    /** {@inheritDoc} */
    public final HealthReport getBuildHealth() {
        return healthReportBuilder.computeHealth(getHealthCounter());
    }

    /**
     * Returns the health counter of this result.
     *
     * @return the health counter of this result.
     */
    protected abstract int getHealthCounter();

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public final Build<?, ?> getOwner() {
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
