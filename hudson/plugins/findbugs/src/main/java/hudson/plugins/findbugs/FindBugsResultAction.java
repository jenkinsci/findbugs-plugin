package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.findbugs.util.ChartBuilder;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.PrioritiesAreaRenderer;
import hudson.plugins.findbugs.util.ResultAreaRenderer;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Controls the live cycle of the FindBugs results. This action persists the
 * results of the FindBugs analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the FindBugs result trend.
 * </p>
 *
 * @author Ulli Hafner
 */
public class FindBugsResultAction implements StaplerProxy, HealthReportingAction {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** URL to results. */
    private static final String FINDBUGS_RESULT_URL = "findbugsResult";
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The actual result of the FindBugs analysis. */
    private FindBugsResult result;
    /** Builds a health report. */
    private final HealthReportBuilder healthReportBuilder;

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param result
     *            the result in this build
     * @param healthReportBuilder
     *            health builder to use
     */
    public FindBugsResultAction(final Build<?, ?> owner, final FindBugsResult result, final HealthReportBuilder healthReportBuilder) {
        this.owner = owner;
        this.result = result;
        this.healthReportBuilder = healthReportBuilder;
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return getResult();
    }

    /**
     * Returns the FindBugs result.
     *
     * @return the FindBugs result
     */
    public FindBugsResult getResult() {
        return result;
    }

    /** {@inheritDoc} */
    public HealthReport getBuildHealth() {
        return healthReportBuilder.computeHealth(getResult().getNumberOfWarnings());
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Result";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        if (result.getNumberOfWarnings() > 0) {
            return FindBugsDescriptor.FINDBUGS_ACTION_LOGO;
        }
        else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return FINDBUGS_RESULT_URL;
    }

    /**
     * Gets the FindBugs result of the previous build.
     *
     * @return the FindBugs result of the previous build.
     * @throws NoSuchElementException if there is no previous build for this action
     */
    public FindBugsResultAction getPreviousResult() {
        FindBugsResultAction previousBuild = getPreviousBuild();
        if (previousBuild == null) {
            throw new NoSuchElementException("There is no previous build for action " + this);
        }
        return previousBuild;
    }

    /**
     * Gets the test result of a previous build, if it's recorded, or <code>null</code> if not.
     *
     * @return the test result of a previous build, or <code>null</code>
     */
    private FindBugsResultAction getPreviousBuild() {
        AbstractBuild<?, ?> build = owner;
        while (true) {
            build = build.getPreviousBuild();
            if (build == null) {
                return null;
            }
            FindBugsResultAction action = build.getAction(FindBugsResultAction.class);
            if (action != null) {
                return action;
            }
        }
    }

    /**
     * Returns whether a previous build already did run with FindBugs.
     *
     * @return <code>true</code> if a previous build already did run with
     *         FindBugs.
     */
    public boolean hasPreviousResult() {
        return getPreviousBuild() != null;
    }

    /**
     * Sets the FindBugs result for this build. The specified result will be persisted in the build folder
     * as an XML file.
     *
     * @param result the result to set
     */
    public void setResult(final FindBugsResult result) {
        this.result = result;
    }

    /**
     * Generates a PNG image for the test result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link FindBugsResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        if (request.checkIfModified(owner.getTimestamp(), response) || healthReportBuilder == null) {
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(), WIDTH, HEIGHT);
    }

    /**
     * Generates a PNG image for the test result trend.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link FindBugsResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doGraphMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        if (request.checkIfModified(owner.getTimestamp(), response) || healthReportBuilder == null) {
            return;
        }
        ChartUtil.generateClickableMap(request, response, createChart(), WIDTH, HEIGHT);
    }

    /**
     * Creates the chart for this action.
     *
     * @return the chart for this action.
     */
    private JFreeChart createChart() {
        ChartBuilder chartBuilder = new ChartBuilder();
        StackedAreaRenderer renderer;
        if (healthReportBuilder.isHealthyReportEnabled() || healthReportBuilder.isFailureThresholdEnabled()) {
            renderer = new ResultAreaRenderer(FINDBUGS_RESULT_URL, "warning");
        }
        else {
            renderer = new PrioritiesAreaRenderer(FINDBUGS_RESULT_URL, "warning");
        }
        return chartBuilder.createChart(buildDataSet(), renderer, healthReportBuilder.getThreshold(),
                healthReportBuilder.isHealthyReportEnabled() || !healthReportBuilder.isFailureThresholdEnabled());
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @return the data set
     */
    private CategoryDataset buildDataSet() {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (FindBugsResultAction action = this; action != null; action = action.getPreviousBuild()) {
            FindBugsResult current = action.getResult();
            List<Integer> series = healthReportBuilder.createSeries(
                    current.getNumberOfHighWarnings(),
                    current.getNumberOfNormalWarnings(),
                    current .getNumberOfLowWarnings());
            int level = 0;
            for (Integer integer : series) {
                builder.add(integer, level, new NumberOnlyBuildLabel(action.owner));
                level++;
            }
        }
        return builder.build();
    }
}
