package hudson.plugins.findbugs.dashboard;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.analysis.graph.BuildResultGraph;
import hudson.plugins.findbugs.FindBugsProjectAction;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A portlet that shows the trend graph of evaluations made on the FindBugs Cloud.
 *
 * @author Keith Lea
 */
public class FindBugsEvaluationsGraphPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link hudson.plugins.findbugs.dashboard.FindBugsEvaluationsGraphPortlet}.
     *
     * @param name
     *            the name of the portlet
     * @param width
     *            width of the graph
     * @param height
     *            height of the graph
     * @param dayCountString
     *            number of days to consider
     */
    @DataBoundConstructor
    public FindBugsEvaluationsGraphPortlet(final String name, final String width, final String height, final String dayCountString) {
        super(name, width, height, dayCountString);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return FindBugsProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "findbugs";
    }

    /** {@inheritDoc} */
    @Override
    protected BuildResultGraph getGraphType() {
        return new FindbugsEvaluationsGraph();
    }

    /**
     * Extension point registration.
     *
     * @author Keith Lea
     */
    @Extension(optional = true)
    public static class WarningsGraphDescriptor extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return "FindBugs Cloud Ccomments";
        }
    }
}
