package hudson.plugins.findbugs.parser.ant;


/**
 * Represents a Java class that contains several warnings.
 */
public class File {
    /** Name of this class. */
    private String classname;
    /** Role of this class. */
    private String role;
    /** The line with the bug. */
    private SourceLine sourceLine;

    /**
     * Adds a new source line to this class.
     *
     * @param sourceLine
     *            the new sourceLine
     */
    public void setSourceLine(final SourceLine sourceLine) {
        if (this.sourceLine == null) {
            this.sourceLine = sourceLine;
        }
    }

    /**
     * Returns the source line of this class.
     *
     * @return the sourceLine of this class
     */
    public SourceLine getSourceLine() {
        return sourceLine;
    }

    /**
     * Sets the role to the specified value.
     *
     * @param role the value to set
     */
    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * Returns the class name.
     *
     * @return the class name
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Sets the class name to the specified value.
     *
     * @param classname the value to set
     */
    public void setClassname(final String classname) {
        this.classname = classname;
    }

    /**
     * Returns whether this class is a role or a class with an error.
     *
     * @return <code>true</code> if this is a role class
     */
    public boolean isRoleClass() {
        return role != null;
    }
}

