package hudson.plugins.findbugs.model;

/**
 * A file annotation is a marker either for a fixed line number of a file or a
 * marker for the file itself. An annotation consists of a description and a
 * tool tip.
 *
 * @author Ulli Hafner
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
     * Returns the unique key of this annotation.
     *
     * @return the unique key of this annotation.
     */
    long getKey();

    /**
     * Returns the priority of this annotation.
     *
     * @return the priority of this annotation
     */
    Priority getPriority();

    /**
     * Returns the workspace file that contains this annotation.
     *
     * @return the workspace file that contains this annotation
     */
    WorkspaceFile getWorkspaceFile();

    /**
     * Sets the workspace file that contains this annotation.
     *
     * @param workspaceFile
     *            the workspace file that contains this annotation
     */
    void setWorkspaceFile(WorkspaceFile workspaceFile);
}
