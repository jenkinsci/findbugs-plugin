package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.AbstractResultAction;
import hudson.plugins.findbugs.util.HealthReportBuilder;
import hudson.plugins.findbugs.util.PluginDescriptor;

import java.util.NoSuchElementException;

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

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     * @param result
     *            the result in this build
     */
    public FindBugsResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder, final FindBugsResult result) {
        super(owner, healthReportBuilder, result);
    }

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     * @param healthReportBuilder
     *            health builder to use
     */
    public FindBugsResultAction(final AbstractBuild<?, ?> owner, final HealthReportBuilder healthReportBuilder) {
        super(owner, healthReportBuilder);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected PluginDescriptor getDescriptor() {
        return FindBugsPublisher.FIND_BUGS_DESCRIPTOR;
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

    /** {@inheritDoc} */
    public String getMultipleItemsTooltip(final int numberOfItems) {
        return Messages.FindBugs_ResultAction_MultipleWarnings(numberOfItems);
    }

    /** {@inheritDoc} */
    public String getSingleItemTooltip() {
        return Messages.FindBugs_ResultAction_OneWarning();
    }
}
