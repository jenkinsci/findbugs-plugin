package hudson.plugins.findbugs.parser.ant;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Java Bean class for a bug collection of the maven FindBugs format.
 */
public class BugCollection {
    /** FindBugs version. */
    private String version;
    /** FindBugs project information. */
    private ProjectInformation projectInformation;
    /** All bug instances of this bug collection. */
    private final Set<BugInstance> bugInstances = new HashSet<BugInstance>();

    /**
     * Adds a new bug instance to this bug collection.
     *
     * @param bugInstance the file to add
     */
    public void addBugInstance(final BugInstance bugInstance) {
        bugInstances.add(bugInstance);
    }

    /**
     * Returns all bug instances of this bug collection. The returned collection is
     * read-only.
     *
     * @return all files of this bug collection
     */
    public Collection<BugInstance> getBugInstances() {
        return Collections.unmodifiableCollection(bugInstances);
    }

    /**
     * Sets the FindBugs project information.
     *
     * @param projectInformation the project information
     */
    public void setProjectInformation(final ProjectInformation projectInformation) {
        this.projectInformation = projectInformation;
    }

    /**
     * Returns the FindBugs project information (of the native file format).
     *
     * @return the FindBugs project information
     */
    public ProjectInformation getProjectInformation() {
        if (projectInformation == null) {
            projectInformation = new ProjectInformation();
        }
        return projectInformation;
    }

    /**
     * Returns the FindBugs version.
     *
     * @return the FindBugs version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version to the specified value.
     *
     * @param version the value to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}

