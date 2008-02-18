package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.LineRange;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A serializable Java Bean class representing an open task.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Bug implements Serializable, FileAnnotation, Comparable<Bug> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5171661552905752370L;
    /** Current task key.  */
    private static long currentKey;
    /** The message of this task. */
    private String message;
    /** The priority of this task. */
    private Priority priority;
    /** Unique key of this task. */
    private long key;
    /** File this annotation is part of. */
    // FIXME: check if we could omit the serialization of this field
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WorkspaceFile workspaceFile;
    /** Bug category. */
    private String category;
    /** Bug type. */
    private String type;
    /** The ordered list of line ranges. */
    private List<LineRange> lineRanges;
    private String fileName;

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    public Bug(final Priority priority, final String message, final String category, final String type,
            final int start, final int end) {
        initialize(priority, message, category, type, start, end);
    }

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param lineNumber
     *            the line number of the warning in the corresponding file
     */
    public Bug(final Priority priority, final String message, final String category, final String type, final int lineNumber) {
        initialize(priority, message, category, type, lineNumber, lineNumber);
    }

    /**
     * Creates a new instance of <code>Bug</code> that has no associated line in code (file warning).
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     */
    public Bug(final Priority priority, final String message, final String category, final String type) {
        initialize(priority, message, category, type, 1, 1);
    }

    /**
     * Initializes this instance
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "hiding"})
    private void initialize(final Priority priority, final String message, final String category, final String type,
            final int start, final int end) {
        this.priority = priority;
        this.message = message;
        this.category = category;
        this.type = type;
        key = currentKey++;

        lineRanges = new ArrayList<LineRange>();
        lineRanges.add(new LineRange(start, end));
    }
    // CHECKSTYLE:ON

    /**
     * Returns the category of the bug.
     *
     * @return the bug category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the bug type.
     *
     * @return the bug type
     */
    public String getType() {
        return type;
    }

    /** {@inheritDoc} */
    public String getMessage() {
        return message;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return FindBugsMessages.getInstance().getMessage(getType());
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

    // FIXME in sync with equals?
    /** {@inheritDoc} */
    public int compareTo(final Bug otherTask) {
        if (key == otherTask.key) {
            return 0;
        }
        else if (key > otherTask.key) {
            return 1;
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((lineRanges == null) ? 0 : lineRanges.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        final Bug other = (Bug)obj;
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        }
        else if (!category.equals(other.category)) {
            return false;
        }
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

    /** {@inheritDoc} */
    public Collection<LineRange> getLineRanges() {
        return Collections.unmodifiableCollection(lineRanges);
    }
}

