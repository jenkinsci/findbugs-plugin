package hudson.plugins.findbugs;

import hudson.model.AbstractProject;
import hudson.plugins.findbugs.util.AbstractProjectAction;

/**
 * Entry point to visualize the FindBugs trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link FindBugsResultAction}.
 *
 * @author Ulli Hafner
 */
public class FindBugsProjectAction extends AbstractProjectAction<FindBugsResultAction> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -654316141132780561L;

    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public FindBugsProjectAction(final AbstractProject<?, ?> project) {
        super(project, FindBugsResultAction.class, FindBugsPublisher.FIND_BUGS_DESCRIPTOR);
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    public String getTrendName() {
        return Messages.FindBugs_Trend_Name();
    }
}

