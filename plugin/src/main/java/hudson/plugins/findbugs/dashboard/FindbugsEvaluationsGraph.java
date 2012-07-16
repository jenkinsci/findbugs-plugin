package hudson.plugins.findbugs.dashboard;

import hudson.plugins.analysis.Messages;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.graph.CategoryBuildResultGraph;
import hudson.plugins.analysis.graph.GraphConfiguration;
import hudson.plugins.analysis.util.BoxRenderer;
import hudson.plugins.analysis.util.CategoryUrlBuilder;
import hudson.plugins.analysis.util.ToolTipBoxRenderer;
import hudson.plugins.analysis.util.ToolTipBuilder;
import hudson.plugins.analysis.util.ToolTipProvider;
import hudson.plugins.findbugs.FindBugsResult;
import hudson.util.ColorPalette;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Builds a review count graph for a specified result action.
 *
 * @author Keith Lea
 */
public class FindbugsEvaluationsGraph extends CategoryBuildResultGraph {
    @Override
    public String getId() {
        return "EVALS";
    }

    @Override
    public String getLabel() {
        return hudson.plugins.findbugs.Messages.FindBugs_EvaluationsGraph_title();
    }

    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        if (current instanceof FindBugsResult) {
            FindBugsResult findBugsResult = (FindBugsResult) current;
            series.add(findBugsResult.getNumberOfComments());
        }
        return series;
    }

    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createBlockChart(dataSet);
    }

    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.BLUE};
    }

    /** {@inheritDoc} */
    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        CategoryUrlBuilder url = new UrlBuilder(getRootUrl(), pluginName);
        ToolTipBuilder toolTip = new DescriptionBuilder(toolTipProvider);
        if (configuration.useBuildDateAsDomain()) {
            return new ToolTipBoxRenderer(toolTip);
        }
        else {
            return new BoxRenderer(url, toolTip);
        }
    }

    /**
     * Shows a tooltip.
     */
    private static final class DescriptionBuilder extends ToolTipBuilder {
        private static final long serialVersionUID = -223463531447822459L;

        DescriptionBuilder(final ToolTipProvider provider) {
            super(provider);
        }

        /** {@inheritDoc} */
        @Override
        protected String getShortDescription(final int row) {
            if (row == 1) {
                return Messages.Trend_Fixed();
            }
            else {
                return Messages.Trend_New();
            }
        }
    }

    /**
     * Shows a URL.
     */
    private static final class UrlBuilder extends CategoryUrlBuilder {
        private static final long serialVersionUID = 6928145843235050754L;

        UrlBuilder(final String rootUrl, final String pluginName) {
            super(rootUrl, pluginName);
        }

        /** {@inheritDoc} */
        @Override
        protected String getDetailUrl(final int row) {
            if (row == 1) {
                return "fixed";
            }
            else {
                return "new";
            }
        }
    }
}
