package hudson.plugins.findbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

// CHECKSTYLE:OFF
public class JavaClass {
    private final List<Warning> warnings = new ArrayList<Warning>();

    public void addWarning(final Warning warning) {
        warnings.add(warning);
    }

    public Collection<Warning> getWarnings() {
        return Collections.unmodifiableCollection(warnings);
    }

    public int getNumberOfWarnings() {
        return warnings.size();
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
}

