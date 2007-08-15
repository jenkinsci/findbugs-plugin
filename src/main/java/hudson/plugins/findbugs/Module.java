package hudson.plugins.findbugs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A Maven module.
 */
public class Module {
    /** All packages with warnings. */
    private final Map<String, JavaPackage> packages = new HashMap<String, JavaPackage>();
    /** FindBugs version. */
    private String version;
    /** FindBugs threshold. */
    private String threshold;
    /** FindBugs effort. */
    private String effort;
    /** name of this module. */
    private String name;

    /**
     * Creates a new instance of <code>Module</code>.
     */
    public Module() {
        name = "undefined";
    }

    /**
     * Creates a new instance of <code>Module</code>.
     *
     * @param name
     *            name of this module
     */
    public Module(final String name) {
        this.name = name;
    }

    /**
     * Adds a new class to this module. If a corresponding package does not yet exists it will be created.
     *
     * @param javaClass the class to add
     */
    public void addClass(final JavaClass javaClass) {
        String packageName = javaClass.getPackage();
        if (packages.containsKey(packageName)) {
            packages.get(packageName).addClass(javaClass);
        }
        else {
            packages.put(packageName, new JavaPackage(javaClass));
        }
    }

    /**
     * Adds a new warning to this module. Additionally adds a corresponding
     * class to this module.
     *
     * @param warning
     *            the new warning
     */
    public void addWarning(final Warning warning) {
        JavaClass javaClass = warning.getJavaClass();
        javaClass.addWarning(warning);
        addClass(javaClass);
    }

    /**
     * Returns the packages in this module. The returned collection is read-only.
     *
     * @return the packages in this module
     */
    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(packages.values());
    }

    /**
     * Returns the total number of warnings in this module.
     *
     * @return the total number of warnings in this module
     */
    public int getNumberOfWarnings() {
        return getWarnings().size();
    }

    /**
     * Returns the warnings in the specified package of this module. The
     * returned collection is read-only.
     *
     * @param packageName
     *            the package to get the warnings for
     * @return the warnings in the specified package of this module
     */
    public Set<Warning> getWarnings(final String packageName) {
        if (packages.containsKey(packageName)) {
            return packages.get(packageName).getWarnings();
        }
        else {
            return Collections.emptySet();
        }
    }

    /**
     * Returns the number of warnings of the specified package.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getNumberOfWarnings(final String packageName) {
        return getWarnings(packageName).size();
    }

    /**
     * Returns all warnings in this module. The returned collection is
     * read-only.
     *
     * @return all warnings in this module
     */
    public Set<Warning> getWarnings() {
        Set<Warning> allWarnings = new HashSet<Warning>();
        for (JavaPackage javaPackage : packages.values()) {
            allWarnings.addAll(javaPackage.getWarnings());
        }
        return Collections.unmodifiableSet(allWarnings);
    }

    /**
     * Returns the version.
     *
     * @return the version
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

    /**
     * Returns the threshold.
     *
     * @return the threshold
     */
    public String getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold to the specified value.
     *
     * @param threshold the value to set
     */
    public void setThreshold(final String threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the effort.
     *
     * @return the effort
     */
    public String getEffort() {
        return effort;
    }

    /**
     * Sets the effort to the specified value.
     *
     * @param effort the value to set
     */
    public void setEffort(final String effort) {
        this.effort = effort;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name to the specified value.
     *
     * @param name the value to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Adds the specified collection of warnings to this module.
     *
     * @param warnings the warnings to add
     */
    public void addWarnings(final Set<Warning> warnings) {
        for (Warning warning : warnings) {
            addWarning(warning);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("")
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        final Module other = (Module)obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}

