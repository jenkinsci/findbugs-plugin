package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;

import java.io.Serializable;

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
    /** Line number of the task in the corresponding file. */
    private int lineNumber;
    /** Unique key of this task. */
    private long key;
    /** Determines whether this is a file annotation or a annotation at a specified line. */
    private boolean hasLineNumber;
    /** File this annotation is part of. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private transient WorkspaceFile workspaceFile;
    /** Bug category. */
    private String category;
    /** Bug type. */
    private String type;

    /**
     * Creates a new instance of <code>Warning</code>.
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
        initialize(priority, message, category, type, true, lineNumber);
    }

    /**
     * Creates a new instance of <code>Warning</code> that has no associated line in code (file warning).
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
        initialize(priority, message, category, type, false, 0);
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
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings({"PMD", "hiding"})
    private void initialize(final Priority priority, final String message, final String category, final String type,
            final boolean hasLineNumber, final int lineNumber) {
        this.priority = priority;
        this.message = message;
        this.category = category;
        this.type = type;
        this.lineNumber = lineNumber;
        this.hasLineNumber = hasLineNumber;
        key = currentKey++;
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

    /** {@inheritDoc} */
    public int getLineNumber() {
        return lineNumber;
    }

    /** {@inheritDoc} */
    public boolean isLineAnnotation() {
        return hasLineNumber;
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
    }

    /** {@inheritDoc} */
    public WorkspaceFile getWorkspaceFile() {
        return workspaceFile;
    }

    // FIXME in synch
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
        int prime = 31;
        int result = 1;
        result = prime * result + (hasLineNumber ? 1231 : 1237);
        result = prime * result + lineNumber;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority.hashCode());
        result = prime * result + ((workspaceFile == null) ? 0 : workspaceFile.hashCode());
        return result;
    }

    // CHECKSTYLE:OFF
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
        if (hasLineNumber != other.hasLineNumber) {
            return false;
        }
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

