package hudson.plugins.findbugs.util;

import hudson.Util;
import hudson.util.StackedAreaRenderer2;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import org.jfree.data.category.CategoryDataset;

/**
 * Renderer that provides direct access to the individual results of a build via
 * links.
 *
 * @author Ulli Hafner
 */
public final class ResultAreaRenderer extends StackedAreaRenderer2 {
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
    public ResultAreaRenderer(final String url, final String name) {
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
        int number = 0;
        for (int index = 0; index < dataset.getRowCount(); index++) {
            final Number value = dataset.getValue(index, column);
            if (value != null) {
                number += value.intValue();
            }
        }
        return String.valueOf(Util.combine(number, name));
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