package hudson.plugins.findbugs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a Java package that contains several classes.
 */
public class JavaPackage implements Serializable, WarningProvider {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -984590362411123375L;
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public int getNumberOfLowWarnings() {
        return WarningDifferencer.countLowPriorityWarnings(getWarnings());
    }

    /** {@inheritDoc} */
    public int getNumberOfHighWarnings() {
        return WarningDifferencer.countHighPriorityWarnings(getWarnings());
    }

    /** {@inheritDoc} */
    public int getNumberOfNormalWarnings() {
        return WarningDifferencer.countNormalPriorityWarnings(getWarnings());
    }
}

