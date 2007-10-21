package hudson.plugins.findbugs.util;

/**
 * A file annotation: an annotation is a marker either for a fixed line number
 * of a file or a marker for the file itself. An annotation consists of a
 * description and a tool tip.
 */
public interface FileAnnotation {
    /**
     * Returns the message of this annotation.
     *
     * @return the message of this annotation
     */
    String getMessage();

    /**
     * Returns the a detailed description that will be used as tool tip.
     *
     * @return the tool tip of this annotation
     */
    String getToolTip();

    /**
     * Returns the line number of this annotation.
     *
     * @return the line number of this annotation.
     */
    int getLineNumber();

    /**
     * Returns whether this annotation is for a specific line or for the whole
     * file.
     *
     * @return <code>true</code> if this annotation is for a specific line of
     *         the file, <code>false</code> if this annotation is for the file
     *         itself.
     */
    boolean isLineAnnotation();

    /**
     * Gets the file name of this annotation. This name could be either a
     * absolute filename or a filename relative to the Hudson folder of the
     * associated build.
     *
     * @return the file name of this annotation
     */
    String getFileName();
}
