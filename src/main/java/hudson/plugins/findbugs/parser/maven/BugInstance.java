package hudson.plugins.findbugs.parser.maven;

import org.apache.commons.lang.StringUtils;

/**
 * Java Bean class for a warning of the maven FindBugs format.
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
    /** Line number of warning. */
    private int lineNumber;
    /** True if this warning is for a valid line number. */
    private boolean hasLineNumber;
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
        return lineNumber;
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
                lineNumber = Integer.valueOf(StringUtils.substringBefore(lineNumberString, RANGE_SEPARATOR));
            }
            else {
                lineNumber = Integer.valueOf(lineNumberString);
            }
            hasLineNumber = lineNumber > 0;
        }
        catch (NumberFormatException exception) {
            lineNumber = 0;
            hasLineNumber = false;
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
     * Checks if this warning represents a line annotation.
     *
     * @return <code>true</code>, if is line annotation, <code>false</code>
     *         if the annotation is for the whole file
     */
    public boolean isLineAnnotation() {
        return hasLineNumber;
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + (hasLineNumber ? 1231 : 1237);
        result = prime * result + lineNumber;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("PMD")
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
        if (hasLineNumber != other.hasLineNumber) {
            return false;
        }
        if (lineNumber != other.lineNumber) {
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

