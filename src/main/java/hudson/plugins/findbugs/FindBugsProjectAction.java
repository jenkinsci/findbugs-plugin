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
        super(project, FindBugsResultAction.class, FindBugsDescriptor.FINDBUGS_ACTION_LOGO, "../lastBuild/findbugsResult");
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "findbugs";
    }

    /** {@inheritDoc} */
    @Override
    protected String getCookieName() {
        return "FindBugs_displayMode";
    }
}

