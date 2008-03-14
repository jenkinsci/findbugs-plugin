package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractResultAction;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.util.DataSetBuilder;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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
public class FindBugsResultAction extends AbstractResultAction<FindBugsResult> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** URL to results. */
    private static final String FINDBUGS_RESULT_URL = "findbugsResult";

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
    public FindBugsResultAction(final AbstractBuild<?, ?> owner, final FindBugsResult result, final HealthReportBuilder healthReportBuilder) {
        super(owner, healthReportBuilder, result);
    }

    /** {@inheritDoc} */
    @Override
    protected int getHealthCounter() {
        return getResult().getNumberOfAnnotations();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return FindBugsDescriptor.FINDBUGS_ACTION_LOGO;
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return FINDBUGS_RESULT_URL;
    }

    /**
     * Gets the FindBugs result of the previous build.
     *
     * @return the FindBugs result of the previous build.
     * @throws NoSuchElementException
     *             if there is no previous build for this action
     */
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

    /**
     * Creates the chart for this action.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the chart for this action.
     */
    @Override
    protected JFreeChart createChart(final StaplerRequest request, final StaplerResponse response) {
        String parameter = request.getParameter("useHealthBuilder");
        boolean useHealthBuilder = Boolean.valueOf(StringUtils.defaultIfEmpty(parameter, "true"));
        return getHealthReportBuilder().createGraph(useHealthBuilder, FINDBUGS_RESULT_URL, buildDataSet(useHealthBuilder),
                Messages.FindBugs_ResultAction_OneWarning(),
                Messages.FindBugs_ResultAction_MultipleWarnings("%d"));
    }

    /**
     * Returns the data set that represents the result. For each build, the
     * number of warnings is used as result value.
     *
     * @param useHealthBuilder
     *            determines whether the health builder should be used to create
     *            the data set
     * @return the data set
     */
    private CategoryDataset buildDataSet(final boolean useHealthBuilder) {
        DataSetBuilder<Integer, NumberOnlyBuildLabel> builder = new DataSetBuilder<Integer, NumberOnlyBuildLabel>();
        for (FindBugsResultAction action = this; action != null; action = action.getPreviousBuild()) {
            FindBugsResult current = action.getResult();
            if (current != null) {
                List<Integer> series;
                if (useHealthBuilder && getHealthReportBuilder().isEnabled()) {
                    series = getHealthReportBuilder().createSeries(current.getNumberOfAnnotations());
                }
                else {
                    series = new ArrayList<Integer>();
                    series.add(current.getNumberOfAnnotations(Priority.LOW));
                    series.add(current.getNumberOfAnnotations(Priority.NORMAL));
                    series.add(current.getNumberOfAnnotations(Priority.HIGH));
                }
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
