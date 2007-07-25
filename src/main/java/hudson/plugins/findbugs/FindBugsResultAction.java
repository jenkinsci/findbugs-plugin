package hudson.plugins.findbugs;

import hudson.model.*;
import hudson.util.*;
import hudson.util.ColorPalette;
import hudson.util.ChartUtil.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;
import org.kohsuke.stapler.*;

/**
 * Controls the live cycle of the FindBugs results. This action persists the
 * results of the FindBugs analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * Moreover, this class renders the FindBugs result trend.
 * </p>
 */
public class FindBugsResultAction implements StaplerProxy, HealthReportingAction {
    /** Height of the graph. */
    private static final int HEIGHT = 200;
    /** Width of the graph. */
    private static final int WIDTH = 500;
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** The associated build of this action. */
    private final Build<?, ?> owner;
    /** The actual result of the FindBugs analysis. */
    private FindBugsResult result;
    /** Report health as 100% when the number of warnings is less than this value. */
    private final int healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final int unHealthy;
    /** Determines whether to use the provided healthy thresholds. */
    private final boolean isHealthyReportEnabled;
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
            return new HealthReport(percentage, numberOfWarnings + " FindBugs warning found.");
        }
        else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Results";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return null; // i.e., don't show the link in the side bar
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
        final CategoryDataset dataset = buildDataSet(request);
        final String relativePath = StringUtils.defaultIfEmpty(request.getParameter("rel"), "");

        final JFreeChart chart = ChartFactory.createStackedAreaChart(
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

        StackedAreaRenderer ar = new StackedAreaRenderer2();
        plot.setRenderer(ar);
        ar.setSeriesPaint(1, ColorPalette.RED);
        ar.setSeriesPaint(0, ColorPalette.BLUE);

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        return chart;
    }
}
