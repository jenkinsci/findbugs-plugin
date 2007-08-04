package hudson.plugins.findbugs;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.awt.Color;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
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
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** The associated build of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The actual result of the FindBugs analysis. */
    private FindBugsResult result;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final int healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final int unHealthy;
    /** Determines whether to use the provided healthy thresholds. */
    private final boolean isHealthyReportEnabled;
    /** Warning threshold. */
    private final int minimumBugs;

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param minimumBugs
     *            Bug threshold to be reached if a build should be considered as unstable.
     * @param isHealthyReportEnabled
     *            Determines whether to use the provided healthy thresholds.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     */
    public FindBugsResultAction(final Build<?, ?> owner, final int minimumBugs, final boolean isHealthyReportEnabled, final int healthy, final int unHealthy) {
        this.owner = owner;
        this.minimumBugs = minimumBugs;
        this.isHealthyReportEnabled = isHealthyReportEnabled;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
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
        if (isHealthyReportEnabled) {
            int numberOfWarnings = getResult().getNumberOfWarnings();
            int percentage;
            if (numberOfWarnings < healthy) {
                percentage = 100;
            }
            else if (numberOfWarnings > unHealthy) {
                percentage = 0;
            }
            else {
                percentage = 100 - ((numberOfWarnings - healthy) * 100 / (unHealthy - healthy));
            }
            return new HealthReport(percentage, "FindBugs: " + numberOfWarnings + " warnings found.");
        }
        else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Result";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return FindBugsDescriptor.FINDBUGS_ACTION_LOGO;
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "findbugsResult";
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

        if (request.checkIfModified(owner.getTimestamp(), response)) {
            return;
        }
        ChartUtil.generateGraph(request, response, createChart(request), WIDTH, HEIGHT);
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
        if (request.checkIfModified(owner.getTimestamp(), response)) {
            return;
        }
        ChartUtil.generateClickableMap(request, response, createChart(request), WIDTH, HEIGHT);
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @param request
     *            Stapler request
     * @return the data set
     */
    private CategoryDataset buildDataSet(final StaplerRequest request) {
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        for (FindBugsResultAction action = this; action != null; action = action.getPreviousBuild()) {
            int numberOfWarnings = action.getResult().getNumberOfWarnings();
            if (minimumBugs > 0) {
                if (numberOfWarnings > minimumBugs) {
                    builder.add(minimumBugs, "threshold", new NumberOnlyBuildLabel(action.owner));
                }
                else {
                    builder.add(numberOfWarnings, "threshold", new NumberOnlyBuildLabel(action.owner));
                }
            }
            if (numberOfWarnings > minimumBugs) {
                builder.add(numberOfWarnings - minimumBugs, "warnings", new NumberOnlyBuildLabel(action.owner));
            }
            else {
                builder.add(0, "warnings", new NumberOnlyBuildLabel(action.owner));
            }
        }
        return builder.build();
    }

    /**
     * Creates the actual chart for the FindBugs trend.
     *
     * @param request
     *            Stapler Request
     * @return the chart
     */
    private JFreeChart createChart(final StaplerRequest request) {
        CategoryDataset dataset = buildDataSet(request);
        JFreeChart chart = ChartFactory.createStackedAreaChart(
            null,                     // chart title
            null,                     // unused
            "count",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            false,                    // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        StackedAreaRenderer renderer = new AreaRenderer();
        plot.setRenderer(renderer);
        renderer.setSeriesPaint(1, ColorPalette.RED);
        renderer.setSeriesPaint(0, ColorPalette.BLUE);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }

    /**
     * Renderer that provides access to the individual FindBugs results.
     */
    static final class AreaRenderer extends StackedAreaRenderer2 {
        /** Unique identifier of this class. */
        private static final long serialVersionUID = -4683951507836348304L;

        /** {@inheritDoc} */
        @Override
        public String generateURL(final CategoryDataset dataset, final int row, final int column) {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
            return label.build.getNumber() + "/findbugsResult/";
        }

        /** {@inheritDoc} */
        @Override
        public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
            NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
            FindBugsResultAction action = label.build.getAction(FindBugsResultAction.class);
            return String.valueOf(Util.combine(action.getResult().getNumberOfWarnings(), "warning"));
        }
    }
}
