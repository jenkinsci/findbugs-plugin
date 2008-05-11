package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.util.model.AbstractAnnotation;
import hudson.plugins.findbugs.util.model.Priority;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Bug extends AbstractAnnotation {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;
    /** Bug category. */
    private final String category;
    /** Bug type. */
    private final String type;
    /** Bug HTML description. */
    private String tooltip = StringUtils.EMPTY;

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    public Bug(final Priority priority, final String message, final String category, final String type,
            final int start, final int end) {
        super(priority, message, start, end);

        this.category = category;
        this.type = type;
    }

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param lineNumber
     *            the line number of the warning in the corresponding file
     */
    public Bug(final Priority priority, final String message, final String category, final String type, final int lineNumber) {
        this(priority, message, category, type, lineNumber, lineNumber);
    }

    /**
     * Creates a new instance of <code>Bug</code> that has no associated line in code (file warning).
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     */
    public Bug(final Priority priority, final String message, final String category, final String type) {
        this(priority, message, category, type, 0, 0);
    }

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     * @param tooltip
     *            the tooltip to show
     */
    public Bug(final Priority priority, final String message, final String category, final String type,
            final int start, final int end, final String tooltip) {
        this(priority, message, category, type, start, end);

        this.tooltip = tooltip;
    }

    /**
     * Returns the category of the bug.
     *
     * @return the bug category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the associated file name of this bug (without path).
     *
     * @return the short file name
     */
    public String getShortFileName() {
        return StringUtils.substringAfterLast(getFileName(), "/");
    }

    /**
     * Returns the bug type.
     *
     * @return the bug type
     */
    public String getType() {
        return type;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return StringUtils.defaultIfEmpty(tooltip, FindBugsMessages.getInstance().getMessage(getType()));
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bug other = (Bug)obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        }
        else if (!category.equals(other.category)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        }
        else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}

