package hudson.plugins.findbugs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// CHECKSTYLE:OFF
public class Module {
    private final Map<String, JavaPackage> packages = new HashMap<String, JavaPackage>();
    private String version;
    private String threshold;
    private String effort;
    private String name;

    /**
     * Creates a new instance of <code>Module</code>.
     */
    public Module() {
        // nothing to do
    }

    /**
     * Creates a new instance of <code>Module</code>.
     * @param string
     */
    public Module(final String string) {
        name = string;
    }

    public void addClass(final JavaClass javaClass) {
        String packageName = javaClass.getPackage();
        if (packages.containsKey(packageName)) {
            packages.get(packageName).addClass(javaClass);
        }
        else {
            packages.put(packageName, new JavaPackage(javaClass));
        }
    }

    public void addWarning(final Warning warning) {
        JavaClass javaClass = warning.getJavaClass();
        javaClass.addWarning(warning);
        addClass(javaClass);
    }

    public Collection<JavaPackage> getPackages() {
        return Collections.unmodifiableCollection(packages.values());
    }

    public int getNumberOfWarnings() {
        int warnings = 0;
        for (JavaPackage javaPackage : packages.values()) {
            warnings += javaPackage.getNumberOfWarnings();
        }
        return warnings;
    }

    public Collection<Warning> getWarnings(final String packageName) {
        if (packages.containsKey(packageName)) {
            return packages.get(packageName).getWarnings();
        }
        else {
            return Collections.emptyList();
        }
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
     * Returns the number of warnings of the specified package.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getNumberOfWarnings(final String packageName) {
        if (packages.containsKey(packageName)) {
            return packages.get(packageName).getNumberOfWarnings();
        }
        return 0;
    }
}

