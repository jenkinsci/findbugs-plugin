package hudson.plugins.findbugs.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *  A base class for annotations.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public abstract class AbstractAnnotation implements FileAnnotation, Serializable, Comparable<AbstractAnnotation> {
    /** Current task key.  */
    private static long currentKey;
    /** The message of this task. */
    private final String message;
    /** The priority of this task. */
    private final Priority priority;
    /** Unique key of this task. */
    private long key;
    /** File this annotation is part of. */
    private WorkspaceFile workspaceFile;
    /** The ordered list of line ranges. */
    private final List<LineRange> lineRanges;
    /** The filename of the class that contains this bug. */
    private String fileName;
    /** Primary line number of this warning, i.e., the start line of the first line range. */
    private final int primaryLineNumber;

    /**
     * Creates a new instance of <code>AbstractAnnotation</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    public AbstractAnnotation(final Priority priority, final String message, final int start, final int end) {
        this.priority = priority;
        this.message = message;

        key = currentKey++;

        lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(start, end));
        primaryLineNumber = start;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    public int compareTo(final AbstractAnnotation otherTask) {
        if (getKey() == otherTask.getKey()) {
            return 0;
        }
        else if (getKey() > otherTask.getKey()) {
            return 1;
        }
        return -1;
    }

    /** {@inheritDoc} */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the unique key of this task.
     *
     * @param key the key
     */
    public void setKey(final long key) {
        this.key = key;
    }

    /**
     * Returns the key of this task.
     *
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * Connects this annotation with the specified workspace file.
     *
     * @param workspaceFile the workspace file that contains this annotation
     */
    public void setWorkspaceFile(final WorkspaceFile workspaceFile) {
        this.workspaceFile = workspaceFile;
        fileName = workspaceFile.getName();
    }

    /** {@inheritDoc} */
    public WorkspaceFile getWorkspaceFile() {
        return workspaceFile;
    }

    /** {@inheritDoc} */
    public String getWorkspaceFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    public Collection<LineRange> getLineRanges() {
        return Collections.unmodifiableCollection(lineRanges);
    }

    /** {@inheritDoc} */
    public int getPrimaryLineNumber() {
        return primaryLineNumber;
    }

    /**
     * Adds another line range to this bug.
     *
     * @param lineRange
     *            the line range to add
     */
    public void addLineRange(final LineRange lineRange) {
        if (!lineRanges.contains(lineRange)) {
            lineRanges.add(lineRange);
        }
    }

    // CHECKSTYLE:OFF

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((lineRanges == null) ? 0 : lineRanges.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + primaryLineNumber;
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((workspaceFile == null) ? 0 : workspaceFile.hashCode());
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
        final AbstractAnnotation other = (AbstractAnnotation)obj;
        if (fileName == null) {
            if (other.fileName != null) {
                return false;
            }
        }
        else if (!fileName.equals(other.fileName)) {
            return false;
        }
        if (lineRanges == null) {
            if (other.lineRanges != null) {
                return false;
            }
        }
        else if (!lineRanges.equals(other.lineRanges)) {
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
        if (primaryLineNumber != other.primaryLineNumber) {
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
        if (workspaceFile == null) {
            if (other.workspaceFile != null) {
                return false;
            }
        }
        else if (!workspaceFile.equals(other.workspaceFile)) {
            return false;
        }
        return true;
    }


}
