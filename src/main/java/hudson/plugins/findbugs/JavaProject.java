package hudson.plugins.findbugs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Unit of work for the FindBugs plug-in. A project consists of one or many maven modules.
 * If maven is not used, then a dummy module is created.
 */
public class JavaProject implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -7352094165496099767L;
    /** The modules of this project. */
    private final Map<String, Module> modules = new HashMap<String, Module>();

    /**
     * Adds a new module to this project.
     *
     * @param module
     *            the module to add
     */
    public final void addModule(final Module module) {
        modules.put(module.getName(), module);
    }


    /**
     * Returns the modules in this project.
     *
     * @return the modules in this project
     */
    public Collection<Module> getModules() {
        ArrayList<Module> nonEmptyModules = new ArrayList<Module>();
        for (Module module : modules.values()) {
            if (module.getNumberOfWarnings() > 0) {
                nonEmptyModules.add(module);
            }
        }
        return nonEmptyModules;
    }

    /**
     * Returns the total number of warnings in this project.
     *
     * @return the total number of warnings in this project
     */
    public int getNumberOfWarnings() {
        return getWarnings().size();
    }

    /**
     * Returns all warnings in this project. The returned collection is
     * read-only.
     *
     * @return all warnings in this project
     */
    public Set<Warning> getWarnings() {
        Set<Warning> warnings = new HashSet<Warning>();
        for (Module module : modules.values()) {
            warnings.addAll(module.getWarnings());
        }
        return warnings;
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
     * Returns the warnings in the specified package of this project. The
     * returned collection is read-only.
     *
     * @param packageName
     *            the package to get the warnings for
     * @return the warnings in the specified package of this project
     */
    public Set<Warning> getWarnings(final String packageName) {
        Set<Warning> warnings = new HashSet<Warning>();
        for (Module module : modules.values()) {
            warnings.addAll(module.getWarnings(packageName));
        }
        return warnings;
    }

    /**
     * Returns whether a module with the specified name already is registered in
     * this project.
     *
     * @param name
     *            module name
     * @return <code>true</code> if a module with the specified name already
     *         is registered in this project.
     */
    public boolean contains(final String name) {
        return modules.containsKey(name);
    }

    /**
     * Returns the module with the specified name.
     *
     * @param name
     *            module name
     * @return the module with the specified name.
     */
    public Module getModule(final String name) {
        if (!contains(name)) {
            throw new NoSuchElementException("No such module registered: " + name);
        }
        return modules.get(name);
    }

    /**
     * Gets the maximum number of warnings in a module.
     *
     * @return the maximum number of warnings
     */
    public int getWarningBound() {
        int tasks = 0;
        for (Module module : modules.values()) {
            tasks = Math.max(tasks, module.getNumberOfWarnings());
        }
        return tasks;
    }


    /**
     * Returns whether the warnings format is Maven file format (without source
     * file information).
     *
     * @return <code>true</code> if the Maven file format is used
     */
    public boolean isMavenFormat() {
        if (getModules().isEmpty()) {
            return true;
        }
        return getModules().iterator().next().isMavenFormat();
    }
}

