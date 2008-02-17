package hudson.plugins.findbugs.parser.ant;


/**
 * Java Bean class to identify the position of a warning in a source file
 * (native format only).
 */
public class SourceLine {
    /** The class name. */
    private String classname;
    /** The first line number of the violation. */
    private int start;
    /** The last line number of the violation. */
    private int end;
    /** The source file name (without path). */
    private String sourcefile;
    /** The source file name (including the path). */
    private String sourcepath;

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
     * Returns the start line of the bug instance.
     *
     * @return the start line of the bug instance
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start line of the bug instance to the specified value.
     *
     * @param start the value to set
     */
    public void setStart(final int start) {
        this.start = start;
    }

    /**
     * Returns the end line of the bug instance.
     *
     * @return the end line of the bug instance
     */
    public int getEnd() {
        return end;
    }

    /**
     * Sets the end line of the bug instance to the specified value.
     *
     * @param end the value to set
     */
    public void setEnd(final int end) {
        this.end = end;
    }

    /**
     * Returns the name of the source file.
     *
     * @return the source file name
     */
    public String getSourcefile() {
        return sourcefile;
    }
    /**
     * Sets the source file name to the specified value.
     *
     * @param sourcefile the value to set
     */
    public void setSourcefile(final String sourcefile) {
        this.sourcefile = sourcefile;
    }

    /**
     * Returns the source path.
     *
     * @return the source path
     */
    public String getSourcepath() {
        return sourcepath;
    }

    /**
     * Sets the source path to the specified value.
     *
     * @param sourcepath the value to set
     */
    public void setSourcepath(final String sourcepath) {
        this.sourcepath = sourcepath;
    }
}

