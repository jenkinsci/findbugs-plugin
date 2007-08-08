package hudson.plugins.findbugs;

import java.util.HashSet;
import java.util.Set;

public class WarningDifferencer {
    public static Set<Warning> getNewWarnings(final Module actual, final Module previous) {
        Set<Warning> warnings = new HashSet<Warning>(actual.getWarnings());
        warnings.removeAll(previous.getWarnings());
        return warnings;
    }

    public static Set<Warning> getFixedWarnings(final Module actual, final Module previous) {
        Set<Warning> warnings = new HashSet<Warning>(previous.getWarnings());
        warnings.removeAll(actual.getWarnings());
        return warnings;
    }
    public static Set<Warning> getNewWarnings(final JavaProject actual, final JavaProject previous) {
        Set<Warning> warnings = new HashSet<Warning>(actual.getWarnings());
        warnings.removeAll(previous.getWarnings());
        return warnings;
    }

    public static Set<Warning> getFixedWarnings(final JavaProject actual, final JavaProject previous) {
        Set<Warning> warnings = new HashSet<Warning>(previous.getWarnings());
        warnings.removeAll(actual.getWarnings());
        return warnings;
    }
}

