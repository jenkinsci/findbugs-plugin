package hudson.plugins.findbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Java package that contains several classes.
 */
public class JavaPackage {
    /** The classes in this package. */
    private final List<JavaClass> classes = new ArrayList<JavaClass>();
    /** Unique name of this package. */
    private final String name;

    /**
     * Creates a new instance of <code>JavaPackage</code>.
     *
     * @param javaClass initial class of this package
     */
    public JavaPackage(final JavaClass javaClass) {
        name = javaClass.getPackage();
        addClass(javaClass);
    }

    /**
     * Adds the specified class to this package.
     *
     * @param javaClass
     *            the class to add
     */
    public final void addClass(final JavaClass javaClass) {
        classes.add(javaClass);
    }

    /**
     * Returns the classes in this package. The returned collection is read-only.
     *
     * @return the classes in this package
     */
    public final Collection<JavaClass> getClasses() {
        return Collections.unmodifiableCollection(classes);
    }

    /**
     * Returns all warnings in this package. The returned collection is
     * read-only.
     *
     * @return all warnings in this package
     */
    public Set<Warning> getWarnings() {
        Set<Warning> warnings = new HashSet<Warning>();
        for (JavaClass javaClass : classes) {
            warnings.addAll(javaClass.getWarnings());
        }
        return Collections.unmodifiableSet(warnings);
    }

    /**
     * Returns the total number of warnings in this package.
     *
     * @return the total number of warnings in this package
     */
    public int getNumberOfWarnings() {
        return getWarnings().size();
    }

    /**
     * Returns the name of this package.
     *
     * @return the name of this package
     */
    public final String getName() {
        return name;
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
}

