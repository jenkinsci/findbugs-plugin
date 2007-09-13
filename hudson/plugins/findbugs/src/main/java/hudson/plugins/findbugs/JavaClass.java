package hudson.plugins.findbugs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a Java class that contains several warnings.
 */
public class JavaClass {
    /** The warnings in this class. */
    private final Set<Warning> warnings = new HashSet<Warning>();
    /** Name of this class. */
    private String classname;
    /** Role of this class. */
    private String role;

    /**
     * Adds a new warning to this class.
     *
     * @param warning
     *            the new warning
     */
    public void addWarning(final Warning warning) {
        warnings.add(warning);
        warning.setQualifiedName(classname);
    }

    /**
     * Sets the role to the specified value.
     *
     * @param role the value to set
     */
    public void setRole(final String role) {
        this.role = role;
    }

    /**
     * Returns all warnings in this class. The returned collection is
     * read-only.
     *
     * @return all warnings in this class
     */
    public Set<Warning> getWarnings() {
        return Collections.unmodifiableSet(warnings);
    }

    /**
     * Returns the total number of warnings with priority LOW in this package.
     *
     * @return the total number of warnings with priority LOW in this package
     */
    public int getNumberOfLowWarnings() {
        return WarningDifferencer.countLowPriorityWarnings(getWarnings());
    }

    /**
     * Returns the total number of warnings with priority HIGH in this package.
     *
     * @return the total number of warnings with priority HIGH in this package
     */
    public int getNumberOfHighWarnings() {
        return WarningDifferencer.countHighPriorityWarnings(getWarnings());
    }

    /**
     * Returns the total number of warnings with priority NORMAL in this package.
     *
     * @return the total number of warnings with priority NORMAL in this package
     */
    public int getNumberOfNormalWarnings() {
        return WarningDifferencer.countNormalPriorityWarnings(getWarnings());
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

    /**
     * Returns the package name of this class.
     *
     * @return the package name of this class
     */
    public String getPackage() {
        return StringUtils.substringBeforeLast(classname, ".");
    }

    /**
     * Returns whether this class is a role or a class with an error.
     *
     * @return <code>true</code> if this is a role class
     */
    public boolean isRoleClass() {
        return role != null;
    }
}

