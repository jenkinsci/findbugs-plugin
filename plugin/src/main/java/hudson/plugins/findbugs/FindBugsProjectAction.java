package hudson.plugins.findbugs;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.findbugs.dashboard.FindbugsEvaluationsGraph;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Entry point to visualize the FindBugs trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link FindBugsResultAction}.
 *
 * @author Ulli Hafner
 */
public class FindBugsProjectAction extends AbstractProjectAction<FindBugsResultAction> {
    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public FindBugsProjectAction(final AbstractProject<?, ?> project) {
        super(project, FindBugsResultAction.class, new FindBugsDescriptor());
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

    @Override
    protected List<BuildResultGraph> getAvailableGraphs() {
        List<BuildResultGraph> list = Lists.newArrayList();
        list.addAll(super.getAvailableGraphs());
        list.add(new FindbugsEvaluationsGraph());
        return list;
    }
}

