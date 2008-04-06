package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
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
 * @param <T>
 *            type of the result of this action
 * @author Ulli Hafner
 */
public abstract class AbstractResultAction<T> implements StaplerProxy, HealthReportingAction, ResultAction<T> {
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final AbstractBuild<?, ?> owner;
    /** Builds a health report. */
    private HealthReportBuilder healthReportBuilder;
    /** The actual result of this action. */
    private T result;

    /**
     * Creates a new instance of <code>AbstractResultAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param result the result of the action
     */
    public AbstractResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder, final T result) {
        super();
        this.owner = owner;
        this.healthReportBuilder = healthReportBuilder;
        this.result = result;
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
    public final AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public final Object getTarget() {
        return getResult();
    }

    /** {@inheritDoc} */
    public final T getResult() {
        return result;
    }

    /** {@inheritDoc} */
    public final void setResult(final T result) {
        this.result = result;
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        if (getHealthCounter() > 0) {
            return getIconUrl();
        }
        return null;
    }

    /**
     * Returns the file name URL of the icon.
     *
     * @return the file name URL of the icon.
     */
    protected abstract String getIconUrl();

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
