package hudson.plugins.findbugs;

// CHECKSTYLE:OFF
public class Warning {
    private String type;
    private String category;
    private String priority;
    private String message;
    private String lineNumber;
    private String classname;
    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * Sets the type to the specified value.
     *
     * @param type the value to set
     */
    public void setType(final String type) {
        this.type = type;
    }
    /**
     * Returns the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    /**
     * Sets the category to the specified value.
     *
     * @param category the value to set
     */
    public void setCategory(final String category) {
        this.category = category;
    }

    /**
     * Sets the priority to the specified value.
     *
     * @param priority the value to set
     */
    public void setPriority(final String priority) {
        this.priority = priority;
    }
    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * Sets the message to the specified value.
     *
     * @param message the value to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }
    /**
     * Returns the lineNumer.
     *
     * @return the lineNumer
     */
    public String getLineNumber() {
        return lineNumber;
    }
    /**
     * Sets the lineNumer to the specified value.
     *
     * @param lineNumber the value to set
     */
    public void setLineNumber(final String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getPriority() {
        return priority;
    }

    public void setClassname(final String classname) {
        this.classname = classname;
    }

    /**
     * Returns the classname.
     *
     * @return the classname
     */
    public String getClassName() {
        return classname;
    }
}

