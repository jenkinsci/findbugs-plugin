package hudson.plugins.findbugs.dashboard;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a review count graph for a specified result action.
 *
 * @author Keith Lea
 */
public class FindbugsEvaluationsGraph extends CategoryBuildResultGraph {
    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "EVALS";
    }

    /** {@inheritDoc} */
    @Override
    public String getLabel() {
        return hudson.plugins.findbugs.Messages.FindBugs_EvaluationsGraph_title();
    }

    /** {@inheritDoc} */
    @Override
    protected List<Integer> computeSeries(final BuildResult current) {
        List<Integer> series = new ArrayList<Integer>();
        if (current instanceof FindBugsResult) {
            FindBugsResult findBugsResult = (FindBugsResult) current;
            series.add(findBugsResult.getNumberOfComments());
        }
        return series;
    }

    /** {@inheritDoc} */
    @Override
    protected JFreeChart createChart(final CategoryDataset dataSet) {
        return createBlockChart(dataSet);
    }

    /** {@inheritDoc} */
    @Override
    protected Color[] getColors() {
        return new Color[] {ColorPalette.BLUE};
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @java.lang.SuppressWarnings("serial")
    @SuppressWarnings("SIC")
    @Override
    protected CategoryItemRenderer createRenderer(final GraphConfiguration configuration, final String pluginName, final ToolTipProvider toolTipProvider) {
        CategoryUrlBuilder url = new CategoryUrlBuilder(getRootUrl(), pluginName) {
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
        };
        ToolTipBuilder toolTip = new ToolTipBuilder(toolTipProvider) {
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
        };
        if (configuration.useBuildDateAsDomain()) {
            return new ToolTipBoxRenderer(toolTip);
        }
        else {
            return new BoxRenderer(url, toolTip);
        }
    }
    // CHECKSTYLE:ON
}
