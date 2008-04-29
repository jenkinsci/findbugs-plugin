package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractResultAction;
import hudson.plugins.findbugs.util.HealthReportBuilder;

import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.JFreeChart;
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
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getIconUrl() {
        return FindBugsDescriptor.ACTION_ICON;
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
        AbstractResultAction<FindBugsResult> previousBuild = getPreviousBuild();
        if (previousBuild instanceof FindBugsResultAction) {
            return (FindBugsResultAction)previousBuild;
        }
        throw new NoSuchElementException("There is no previous build for action " + this);
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
        return getHealthReportBuilder().createGraph(useHealthBuilder, FINDBUGS_RESULT_URL,
                buildDataSet(useHealthBuilder),
                Messages.FindBugs_ResultAction_OneWarning(),
                Messages.FindBugs_ResultAction_MultipleWarnings("%d"));
    }
}
