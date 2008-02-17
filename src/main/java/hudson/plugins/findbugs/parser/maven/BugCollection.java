package hudson.plugins.findbugs.parser.maven;

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
    /** All files of this bug collection. */
    private final Set<File> files = new HashSet<File>();

    /**
     * Adds a new file to this bug collection.
     *
     * @param file the file to add
     */
    public void addFile(final File file) {
        files.add(file);
    }

    /**
     * Returns all files of this bug collection. The returned collection is
     * read-only.
     *
     * @return all files of this bug collection
     */
    public Collection<File> getFiles() {
        return Collections.unmodifiableCollection(files);
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

