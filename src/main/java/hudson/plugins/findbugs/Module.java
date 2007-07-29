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

    public void addClass(final JavaClass javaClass) {
        String packageName = javaClass.getPackage();
        if (packages.containsKey(packageName)) {
            packages.get(packageName).addClass(javaClass);
        }
        else {
            packages.put(packageName, new JavaPackage(javaClass));
        }
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
}

