package hudson.plugins.findbugs.parser.ant;

import org.apache.commons.lang.StringUtils;

/**
 * Java Bean class for a warning of the native FindBugs format.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class BugInstance {
    /** Type of warning. */
    private String type;
    /** Category of warning. */
    private String category;
    /** Priority of warning. */
    private String priority;
    /** Message of warning. */
    private String message;
    /** Associated file of this bug instance. */
    private File file;
    /** Line number of warning. */
    private int lineNumber;

    /**
     * Sets the associated class of this bug instance. This class is only
     * considered if it is not a role class and if it is the first class.
     *
     * @param file
     *            the associated class of this bug instance
     */
    public void setFile(final File file) {
        if (!file.isRoleClass() && this.file == null) {
            this.file = file;
        }
    }

    /**
     * Returns the associated class of this bug instance.
     *
     * @return the associated class of this bug instance
     */
    public File getFile() {
        return file;
    }

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
     * Returns whether this bug instance has an associated line number.
     *
     * @return <code>true</code> if this bug instance has an associated line number
     */
    public boolean isLineAnnotation() {
        if (file == null) {
            return false;
        }
        SourceLine sourceLine = file.getSourceLine();
        if (sourceLine == null) {
            return false;
        }
        lineNumber = sourceLine.getStart();
        return true;
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
     * Returns the file name for this warning.
     *
     * @return the file name
     */
    public String getFileName() {
        if (file == null || file.getSourceLine() == null) {
            return "No File";
        }
        return file.getSourceLine().getSourcepath();
    }

    /**
     * Returns the package name of this warning.
     *
     * @return the package name
     */
    public String getPackageName() {
        if (file == null) {
            return "No Package";
        }
        return StringUtils.substringBeforeLast(file.getClassname(), ".");
    }
}

