package hudson.plugins.findbugs.parser.maven;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Java Bean class for a file of the maven FindBugs format.
 *
 * @author Ulli Hafner
 */
public class File {
    /** Name of this class. */
    private String classname;
    /** All bugs in this file. */
    private final Set<BugInstance> bugInstances = new HashSet<BugInstance>();

    /**
     * Adds a new bug instance to this class.
     *
     * @param bugInstance
     *            the new bug instance
     */
    public void addBugInstance(final BugInstance bugInstance) {
        bugInstances.add(bugInstance);
    }

    /**
     * Returns all bug instances of this file. The returned collection is
     * read-only.
     *
     * @return all warnings in this class
     */
    public Collection<BugInstance> getBugInstances() {
        return Collections.unmodifiableCollection(bugInstances);
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((classname == null) ? 0 : classname.hashCode());
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
        final File other = (File)obj;
        if (classname == null) {
            if (other.classname != null) {
                return false;
            }
        }
        else if (!classname.equals(other.classname)) {
            return false;
        }
        return true;
    }
}

