package hudson.plugins.findbugs.util;

import hudson.Util;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. The renderer also displays tooltips for each selected build.
 * <ul>
 * <li>The tooltip is computed per column (i.e., per build) and row (i.e., priority) and shows the
 * number of annotations of the selected priority for this build.</li>
 * <li>The link is also computed per column and links to the results for this
 * build.</li>
 * </ul>
 *
 * @author Ulli Hafner
 */
// TODO: the link should be aware of the priorities and filter the selected priority
public final class PrioritiesAreaRenderer extends StackedAreaRenderer2 {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -4683951507836348304L;
    /** Base URL of the graph links. */
    private final String url;
    /** Name of the shown items. */
    private final String name;

    /**
     * Creates a new instance of <code>AreaRenderer</code>.
     *
     * @param url base URL of the graph links
     * @param name name of the shown items
     */
    public PrioritiesAreaRenderer(final String url, final String name) {
        super();
        this.url = "/" + url + "/";
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return getLabel(dataset, column).build.getNumber() + url;
    }

    /** {@inheritDoc} */
    @Override
    public String generateToolTip(final CategoryDataset dataset, final int row, final int column) {
        String prefix;
        if (row == 2) {
            prefix = "high priority ";
        }
        else if (row == 1) {
            prefix = "normal ";
        }
        else {
            prefix = "low priority ";
        }
        return String.valueOf(Util.combine(dataset.getValue(row, column).intValue(), prefix + name));
    }

    /**
     * Returns the Hudson build label at the specified column.
     *
     * @param dataset dataset of values
     * @param column the column
     * @return the label of the column
     */
    private NumberOnlyBuildLabel getLabel(final CategoryDataset dataset, final int column) {
        return (NumberOnlyBuildLabel)dataset.getColumnKey(column);
    }
}