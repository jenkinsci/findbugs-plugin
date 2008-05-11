package hudson.plugins.findbugs.parser.maven;

import org.apache.commons.lang.StringUtils;

/**
 * Java Bean class for a warning of the maven FindBugs format.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class BugInstance {
    /** Separator of a line number range. */
    private static final String RANGE_SEPARATOR = "-";
    /** Type of warning. */
    private String type;
    /** Category of warning. */
    private String category;
    /** Priority of warning. */
    private String priority;
    /** Message of warning. */
    private String message;
    /** The first line of the warning range. */
    private int start;
    /** The last line of the warning range. */
    private int end;
    /**
     * An expression identifying a line number. Currently supported expressions
     * are single integers (line numbers) or integer ranges (line number ranges,
     * the first value is used as link).
     */
    private String lineNumberExpression;

    /**
     * Returns the type of the warning.
     *
     * @return the type of the warning
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the warning to the specified value.
     *
     * @param type the value to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the category of the warning.
     *
     * @return the category of the warning
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the warning to the specified value.
     *
     * @param category the value to set
     */
    public void setCategory(final String category) {
        this.category = category;
    }

    /**
     * Gets the priority of the warning.
     *
     * @return the priority of the warning
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the warning to the specified value.
     *
     * @param priority the value to set
     */
    public void setPriority(final String priority) {
        this.priority = priority;
    }

    /**
     * Returns the message of the warning.
     *
     * @return the message of the warning
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message of the warning to the specified value.
     *
     * @param message the value to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Gets the line number of the warning.
     *
     * @return the line number of the warning
     */
    public int getLineNumber() {
        return start;
    }

    /**
     * Sets the line number to the specified value.
     *
     * @param lineNumberString the string value of the line number
     */
    public void setLineNumberExpression(final String lineNumberString) {
        lineNumberExpression = lineNumberString;
        try {
            if (lineNumberString.contains(RANGE_SEPARATOR)) {
                start = Integer.valueOf(StringUtils.substringBefore(lineNumberString, RANGE_SEPARATOR));
                end = Integer.valueOf(StringUtils.substringAfter(lineNumberString, RANGE_SEPARATOR));
            }
            else {
                start = Integer.valueOf(lineNumberString);
                end = Integer.valueOf(lineNumberString);
            }
        }
        catch (NumberFormatException exception) {
            start = 0;
            end = 0;
        }
    }

    /**
     * Returns the line number expression. Currently supported expressions are
     * single integers (line numbers) or integer ranges (line number ranges, the
     * first value is used as link).
     *
     * @return the line number expression.
     */
    public String getLineNumberExpression() {
        return lineNumberExpression;
    }

    /**
     * Returns the first line of the warning range.
     *
     * @return the first line of the warning range
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the last line of the warning range.
     *
     * @return the last line of the warning range
     */
    public int getEnd() {
        return end;
    }

    // CHECKSTYLE:OFF

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + end;
        result = prime * result
                + ((lineNumberExpression == null) ? 0 : lineNumberExpression.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + start;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BugInstance other = (BugInstance)obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        }
        else if (!category.equals(other.category)) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        if (lineNumberExpression == null) {
            if (other.lineNumberExpression != null) {
                return false;
            }
        }
        else if (!lineNumberExpression.equals(other.lineNumberExpression)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        }
        else if (!message.equals(other.message)) {
            return false;
        }
        if (priority == null) {
            if (other.priority != null) {
                return false;
            }
        }
        else if (!priority.equals(other.priority)) {
            return false;
        }
        if (start != other.start) {
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

