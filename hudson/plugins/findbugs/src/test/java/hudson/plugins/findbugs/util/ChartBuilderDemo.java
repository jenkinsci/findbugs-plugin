package hudson.plugins.findbugs.util;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 *  Creates and shows a chart demo.
 */
public final class ChartBuilderDemo {
    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    public static void createAndShowGUI() {
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("row", "column",
                new double[][] {{100, 200, 300, 200}, {200, 200, 400, 150}, {50, 100, 400, 200}});
        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createChart(dataset, new StackedAreaRenderer(), 50, true);
        chartBuilder.annotateThreshold(chart, dataset, 0);
        ChartFrame frame = new ChartFrame("Hallo", chart);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     *  Creates and shows a chart demo.
     *
     *  @param args arguments
     */
    public static void main(final String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    /**
     * Creates a new instance of <code>ChartBuilderDemo</code>.
     */
    private ChartBuilderDemo() {
        // no instance
    }
}
