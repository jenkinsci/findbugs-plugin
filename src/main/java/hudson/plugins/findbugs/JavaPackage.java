package hudson.plugins.findbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// CHECKSTYLE:OFF
public class JavaPackage {
    private final List<JavaClass> classes = new ArrayList<JavaClass>();
    private final String name;

    /**
     * Creates a new instance of <code>JavaPackage</code>.
     */
    public JavaPackage(final JavaClass javaClass) {
        name = javaClass.getPackage();
        addClass(javaClass);
    }

    public final void addClass(final JavaClass javaClass) {
        classes.add(javaClass);
    }

    public final Collection<JavaClass> getClasses() {
        return Collections.unmodifiableCollection(classes);
    }

    public final String getName() {
        return name;
    }

    public int getNumberOfWarnings() {
        int warnings = 0;
        for (JavaClass javaClass : classes) {
            warnings += javaClass.getNumberOfWarnings();
        }
        return warnings;
    }

    public int getNumberOfLowWarnings() {
        int warnings = 0;
        for (JavaClass javaClass : classes) {
            warnings += javaClass.getNumberOfLowWarnings();
        }
        return warnings;
    }

    public int getNumberOfHighWarnings() {
        int warnings = 0;
        for (JavaClass javaClass : classes) {
            warnings += javaClass.getNumberOfHighWarnings();
        }
        return warnings;
    }

    public int getNumberOfNormalWarnings() {
        int warnings = 0;
        for (JavaClass javaClass : classes) {
            warnings += javaClass.getNumberOfNormalWarnings();
        }
        return warnings;
    }
}

