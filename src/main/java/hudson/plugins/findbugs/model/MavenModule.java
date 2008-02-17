package hudson.plugins.findbugs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a maven module.
 *
 * @author Ulli Hafner
 */
public class MavenModule extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5467122420572804130L;
    /** Name of this module. */
    private String name;
    /** All Java packages in this maven module (mapped by their name). */
    private final Map<String, JavaPackage> packageMapping = new HashMap<String, JavaPackage>();

    /**
     * Creates a new instance of <code>MavenModule</code>.
     */
    public MavenModule() {
        super();
    }

    /**
     * Creates a new instance of <code>MavenModule</code>.
     *
     * @param moduleName
     *            name of the module
     */
    public MavenModule(final String moduleName) {
        this();

        name = moduleName;
    }

    /**
     * Returns the module name.
     *
     * @return the module name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this module.
     *
     * @param name the name of this module
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildPriorities();
        return this;
    }

    /**
     * Creates the mapping of packages.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        WorkspaceFile file = annotation.getWorkspaceFile();

        String packageName = StringUtils.defaultIfEmpty(file.getPackageName(), "No Package");
        if (!packageMapping.containsKey(packageName)) {
            packageMapping.put(packageName, new JavaPackage(packageName));
        }
        packageMapping.get(packageName).addAnnotation(annotation);
    }

    /**
     * Gets the packages of this module that have annotations.
     *
     * @return the packages with annotations
     */
    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(packageMapping.values());
    }

    /**
     * Gets the package with the given name.
     *
     * @param packageName
     *            the name of the package
     * @return the package with the given name
     */
    public JavaPackage getPackage(final String packageName) {
        JavaPackage javaPackage = packageMapping.get(packageName);
        if (javaPackage != null) {
            return javaPackage;
        }
        throw new NoSuchElementException("Package not found: " + packageName);
    }

    /**
     * Gets the files of this module that have annotations.
     *
     * @return the files with annotations
     */
    public Collection<WorkspaceFile> getFiles() {
        List<WorkspaceFile> packages = new ArrayList<WorkspaceFile>();
        for (JavaPackage javaPackage : packageMapping.values()) {
            packages.addAll(javaPackage.getFiles());
        }
        return packages;
    }

    /**
     * Returns the file with the given name. This method is only valid for
     * single package modules.
     *
     * @param fileName the file name
     * @return the file with the given name.
     */
    public WorkspaceFile getFile(final String fileName) {
        if (packageMapping.size() != 1) {
            throw new IllegalArgumentException("Number of modules != 1");
        }
        return packageMapping.values().iterator().next().getFile(fileName);
    }

    /**
     * Gets the maximum number of tasks in a package of this module.
     *
     * @return the maximum number of tasks
     */
    public int getAnnotationBound() {
        int tasks = 0;
        for (JavaPackage javaPackage : packageMapping.values()) {
            tasks = Math.max(tasks, javaPackage.getNumberOfAnnotations());
        }
        return tasks;
    }
}

