package hudson.plugins.findbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// CHECKSTYLE:OFF
public class JavaProject {
    private final List<Module> modules = new ArrayList<Module>();

    public final void addModule(final Module module) {
        modules.add(module);
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(modules);
    }

    public int getNumberOfWarnings() {
        int warnings = 0;
        for (Module module : modules) {
            warnings += module.getNumberOfWarnings();
        }
        return warnings;
    }
}

