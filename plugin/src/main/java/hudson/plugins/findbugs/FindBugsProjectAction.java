package hudson.plugins.findbugs;

import hudson.model.AbstractProject;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.findbugs.dashboard.FindbugsEvaluationsGraph;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Entry point to visualize the FindBugs trend graph in the project screen.
 * Drawing of the graph is delegated to the associated {@link ResultAction}.
 *
 * @author Ulli Hafner
 */
public class FindBugsProjectAction extends AbstractProjectAction<ResultAction<FindBugsResult>> {
    /**
     * Instantiates a new {@link FindBugsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     */
    public FindBugsProjectAction(final AbstractProject<?, ?> project) {
        this(project, FindBugsResultAction.class);
    }

    /**
     * Instantiates a new {@link FindBugsProjectAction}.
     *
     * @param project
     *            the project that owns this action
     * @param type
     *            the result action type
     */
    public FindBugsProjectAction(final AbstractProject<?, ?> project,
            final Class<? extends ResultAction<FindBugsResult>> type) {
        super(project, type, Messages._FindBugs_ProjectAction_Name(), Messages._FindBugs_Trend_Name(),
                FindBugsDescriptor.PLUGIN_ID, FindBugsDescriptor.ICON_URL, FindBugsDescriptor.RESULT_URL);
    }

    @Override
    protected List<BuildResultGraph> getAvailableGraphs() {
        List<BuildResultGraph> list = Lists.newArrayList();
        list.addAll(super.getAvailableGraphs());
        list.add(new FindbugsEvaluationsGraph());
        return list;
    }
}

