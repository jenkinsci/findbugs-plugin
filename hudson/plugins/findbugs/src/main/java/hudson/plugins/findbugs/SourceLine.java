package hudson.plugins.findbugs;

import java.io.Serializable;

/**
 * Java Bean class to identify the position of a warning in a source file
 * (native format only).
 */
public class SourceLine implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -1295600457503624679L;
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
     * Returns the classname.
     *
     * @return the classname
     */
    public String getClassname() {
        return classname;
    }
    /**
     * Sets the classname to the specified value.
     *
     * @param classname the value to set
     */
    public void setClassname(final String classname) {
        this.classname = classname;
    }
    /**
     * Returns the start.
     *
     * @return the start
     */
    public int getStart() {
        return start;
    }
    /**
     * Sets the start to the specified value.
     *
     * @param start the value to set
     */
    public void setStart(final int start) {
        this.start = start;
    }
    /**
     * Returns the end.
     *
     * @return the end
     */
    public int getEnd() {
        return end;
    }
    /**
     * Sets the end to the specified value.
     *
     * @param end the value to set
     */
    public void setEnd(final int end) {
        this.end = end;
    }
    /**
     * Returns the sourcefile.
     *
     * @return the sourcefile
     */
    public String getSourcefile() {
        return sourcefile;
    }
    /**
     * Sets the sourcefile to the specified value.
     *
     * @param sourcefile the value to set
     */
    public void setSourcefile(final String sourcefile) {
        this.sourcefile = sourcefile;
    }
    /**
     * Returns the sourcepath.
     *
     * @return the sourcepath
     */
    public String getSourcepath() {
        return sourcepath;
    }
    /**
     * Sets the sourcepath to the specified value.
     *
     * @param sourcepath the value to set
     */
    public void setSourcepath(final String sourcepath) {
        this.sourcepath = sourcepath;
    }
}

