package hudson.plugins.findbugs.util;

import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links. This renderer does not render tooltips, these need to be defined in
 * sub-classes.
 *
 * @author Ulli Hafner
 */
public abstract class AbstractAreaRenderer extends StackedAreaRenderer2 {
    /** Base URL of the graph links. */
    private final String url;
    /** Tooltip to be shown if there is one item. */
    private final String singleTooltip;
    /** Tooltip to be shown if there are multiple items. */
    private final String multipleTooltip;

    /**
     * Creates a new instance of <code>AbstractAreaRenderer</code>.
     *
     * @param url
     *            base URL of the graph links
     * @param singleTooltip
     *            tooltip if there is one item
     * @param multipleTooltip
     *            tooltip if there are multiple items
     */
    public AbstractAreaRenderer(final String url, final String singleTooltip, final String multipleTooltip) {
        super();

        this.url = "/" + url + "/";
        this.singleTooltip = singleTooltip;
        this.multipleTooltip = multipleTooltip;
    }

    /** {@inheritDoc} */
    @Override
    public String generateURL(final CategoryDataset dataset, final int row, final int column) {
        return getLabel(dataset, column).build.getNumber() + url;
    }

    /**
     * Returns the tooltip if there is one item.
     *
     * @return the tooltip if there is one item
     */
    public String getSingleTooltip() {
        return singleTooltip;
    }

    /**
     * Returns the tooltip if there are multiple items.
     *
     * @param number
     *            number of items
     * @return the tooltip if there are multiple items
     */
    public String getMultipleTooltip(final int number) {
        return String.format(multipleTooltip, number);
    }

    /**
     * Returns the Hudson build label at the specified column.
     *
     * @param dataset
     *            data set of values
     * @param column
     *            the column
     * @return the label of the column
     */
    private NumberOnlyBuildLabel getLabel(final CategoryDataset dataset, final int column) {
        return (NumberOnlyBuildLabel)dataset.getColumnKey(column);
    }
}
