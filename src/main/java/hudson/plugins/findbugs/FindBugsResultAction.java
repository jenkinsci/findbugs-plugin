package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.findbugs.util.AbstractResultAction;
import hudson.plugins.findbugs.util.ChartBuilder;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.PrioritiesAreaRenderer;
import hudson.plugins.findbugs.util.ResultAction;
import hudson.plugins.findbugs.util.ResultAreaRenderer;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.util.List;
import java.util.NoSuchElementException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerProxy;

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
public class FindBugsResultAction extends AbstractResultAction implements StaplerProxy, HealthReportingAction, ResultAction<FindBugsResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** URL to results. */
    private static final String FINDBUGS_RESULT_URL = "findbugsResult";
    /** The actual result of the FindBugs analysis. */
    private FindBugsResult result;
    /** Builds a health report. */
    private HealthReportBuilder healthReportBuilder;

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
        super(owner);
        this.result = result;
        this.healthReportBuilder = healthReportBuilder;
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return getResult();
    }

    /** {@inheritDoc} */
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
     * Returns the URL for the results of the last build.
     *
     * @return URL for the results of the last build
     */
    public static String getLatestUrl() {
        return "../lastBuild/" + FINDBUGS_RESULT_URL;
    }

    /** {@inheritDoc} */
    public FindBugsResultAction getPreviousResultAction() {
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
        AbstractBuild<?, ?> build = getOwner();
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

    /** {@inheritDoc} */
    public boolean hasPreviousResultAction() {
        return getPreviousBuild() != null;
    }

    /** {@inheritDoc} */
    public void setResult(final FindBugsResult result) {
        this.result = result;
    }

    /**
     * Creates the chart for this action.
     *
     * @return the chart for this action.
     */
    @Override
    protected JFreeChart createChart() {
        ChartBuilder chartBuilder = new ChartBuilder();
        if (healthReportBuilder == null) {
            healthReportBuilder = new HealthReportBuilder("FindBugs", "warning", false, 0, false, 0, 0);
        }
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
            if (current != null) {
                List<Integer> series = healthReportBuilder.createSeries(
                        current.getNumberOfHighWarnings(),
                        current.getNumberOfNormalWarnings(),
                        current .getNumberOfLowWarnings());
                int level = 0;
                for (Integer integer : series) {
                    builder.add(integer, level, new NumberOnlyBuildLabel(action.getOwner()));
                    level++;
                }
            }
        }
        return builder.build();
    }
}
