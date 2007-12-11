package hudson.plugins.findbugs;

import hudson.plugins.findbugs.util.FileAnnotation;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * A FindBugs warning.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Warning implements Serializable, FileAnnotation {
    /** Separator of a line number range. */
    private static final String RANGE_SEPARATOR = "-";
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3694883222707674470L;
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
    /** Corresponding Java class. */
    private JavaClass javaClass;
    /** Corresponding qualified class name. */
    private String qualifiedName;
    /** Filename of the java source. */
    private String fileName;
    /** Unique key of this warning. */
    private int key;
    /** True if this warning is for a valid line number. */
    private boolean hasLineNumber;
    /**
     * An expression identifying a line number. Currently supported expressions
     * are single integers (line numbers) or integer ranges (line number ranges,
     * the first value is used as link).
     */
    private String lineNumberExpression;
    /** The field name of the field associated with this warning. */
    private String fieldName;    

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Links this warning to the specified class. This class is only considered if it
     * is not a role class and if it is the first class.
     *
     * @param owningClass the class that contains this warning
     */
    public void linkClass(final JavaClass owningClass) {
        if (!owningClass.isRoleClass() && javaClass == null) {
            javaClass = owningClass;
            setQualifiedName(owningClass.getClassname());
            setLineNumberExpression(javaClass.getLineNumber());
            setFile(javaClass.getFileName());
        }
    }

    /**
     * Returns the javaClass.
     *
     * @return the javaClass
     */
    public JavaClass getJavaClass() {
        return javaClass;
    }

    /**
     * Adds a source line associated with this warning.
     *
     * @param sourceLine the SourceLine object describing the source line.
     */
    public void addSourceLine(final SourceLine sourceLine) {
        String role = sourceLine.getRole();
        if (sourceLine != null && (role == null || role.length() == 0)) {
            this.lineNumber = sourceLine.getStart();
        }
    }    
    
    /**
     * Adds a field that is associated with this warning.
     *
     * @param field The Field class that describes the field that this
     * warning is about.
     */
    public void addField(final Field field) {
        this.fieldName = field.getName();
    }    

    /** {@inheritDoc} */
    public String getToolTip() {
        return FindBugsMessages.getInstance().getMessage(getType());
    }

    /**
     * Sets the type to the specified value.
     *
     * @param type the value to set
     */
    public void setType(final String type) {
        this.type = type;
        if (this.message == null || this.message.length() == 0) {
            this.message = FindBugsMessages.getInstance().getMessage(type);
        }
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

    /** {@inheritDoc} */
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
     * Gets the priority.
     *
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Returns the qualifiedName.
     *
     * @return the qualifiedName
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns the qualifiedName.
     *
     * @return the qualifiedName
     */
    public String getPackageName() {
        return StringUtils.substringBeforeLast(qualifiedName, ".");
    }

    /**
     * Returns the classname.
     *
     * @return the classname
     */
    public String getClassname() {
        return StringUtils.substringAfterLast(qualifiedName, ".");
    }

    /**
     * Returns a unique key for this warning.
     *
     * @return a unique key for this warning
     */
    public int getKey() {
        return key;
    }

    /**
     * Sets a unique key for this warning.
     *
     * @param key
     *            the unique key of this warning
     */
    public void setKey(final int key) {
        this.key = key;
    }

    /**
     * Sets the fully qualified name of the containing class.
     *
     * @param name
     *            the name of the class
     */
    public void setQualifiedName(final String name) {
        qualifiedName = name;
    }

    /**
     * Sets a reference to the file where this warning is found.
     *
     * @param file the file name
     */
    public void setFile(final String file) {
        fileName = file.replace('\\', '/');
    }

    /** {@inheritDoc} */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns whether a valid filename is available for this warning.
     *
     * @return <code>true</code> if a valid filename is available for this
     *         warning
     */
    public boolean hasFile() {
        return fileName != null;
    }

    /** {@inheritDoc} */
    public boolean isLineAnnotation() {
        return hasLineNumber;
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + lineNumber;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
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
        final Warning other = (Warning)obj;
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
        if (qualifiedName == null) {
            if (other.qualifiedName != null) {
                return false;
            }
        }
        else if (!qualifiedName.equals(other.qualifiedName)) {
            return false;
        }
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        }
        else if (!fieldName.equals(other.fieldName)) {
            return false;
        }        
        return true;
    }
}

