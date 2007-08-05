package hudson.plugins.findbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

// CHECKSTYLE:OFF
public class JavaClass {
    private final List<Warning> warnings = new ArrayList<Warning>();
    private int low;
    private int high;
    private int normal;
    private String role;

    public void addWarning(final Warning warning) {
        warnings.add(warning);
        warning.setClassname(StringUtils.substringAfterLast(classname, "."));
        if ("low".equalsIgnoreCase(warning.getPriority())) {
            low++;
        }
        else if ("high".equalsIgnoreCase(warning.getPriority())) {
            high++;
        }
        else {
            normal++;
        }
    }

    /**
     * Sets the role to the specified value.
     *
     * @param role the value to set
     */
    public void setRole(final String role) {
        this.role = role;
    }

    public Collection<Warning> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    public int getNumberOfWarnings() {
        return warnings.size();
    }

    public int getNumberOfLowWarnings() {
        return low;
    }

    public int getNumberOfHighWarnings() {
        return high;
    }

    public int getNumberOfNormalWarnings() {
        return normal;
    }

    private String classname;

    /**
     * Returns the classname.
     *
     * @return the classname
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Sets the classname to the specified value.
     *
     * @param classname the value to set
     */
    public void setClassname(final String classname) {
        this.classname = classname;
    }

    public String getPackage() {
        return StringUtils.substringBeforeLast(classname, ".");
    }

    /**
     * Returns whether this class is a role or a class with an error.
     * @return <code>true</code> if this is a role class
     */
    public boolean isRoleClass() {
        return role != null;
    }
}

