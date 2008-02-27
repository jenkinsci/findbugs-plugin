package hudson.plugins.findbugs.util.model;

import org.apache.commons.lang.StringUtils;

/**
 * A serializable Java Bean class representing a file in the Hudson workspace.
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class WorkspaceFile extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 601361940925156719L;
    /** The absolute filename of this file. */
    private String name;
    /** Package name of this task. */
    private String packageName;
    /** Module name of this task. */
    private String moduleName;

    /**
     * Returns the filename name of this file.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a bidirectional link between annotation and file.
     *
     * @param annotation
     *            the added annotation
     */
    @Override
    protected void annotationAdded(final FileAnnotation annotation) {
        annotation.setWorkspaceFile(this);
    }

    /**
     * Sets the name of this file.
     *
     * @param name the name of this file
     */
    public void setName(final String name) {
        this.name = name.replace('\\', '/');
    }

    /**
     * Returns a readable name of this workspace file.
     *
     * @return a readable name of this workspace file.
     */
    public String getShortName() {
        return StringUtils.substringAfterLast(name, "/");
    }

    /**
     * Sets the package name to the specified value.
     *
     * @param packageName the package name
     */
    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the packageName.
     *
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the module name to the specified value.
     *
     * @param moduleName the module name
     */
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    /**
     * Returns the moduleName.
     *
     * @return the moduleName
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Creates the bidirectional links between the annotations and this
     * workspace file.
     */
    public void linkAnnotations() {
        for (FileAnnotation annotation : getAnnotations()) {
            annotation.setWorkspaceFile(this);
        }
    }

    /**
     * Rebuilds the bidirectional links between the annotations and this
     * workspace file after deserialization.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildPriorities();
        linkAnnotations();
        return this;
    }

    // CHECKSTYLE:OFF
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("PMD")
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
        final WorkspaceFile other = (WorkspaceFile)obj;
        if (moduleName == null) {
            if (other.moduleName != null) {
                return false;
            }
        }
        else if (!moduleName.equals(other.moduleName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        }
        else if (!packageName.equals(other.packageName)) {
            return false;
        }
        return true;
    }
}

