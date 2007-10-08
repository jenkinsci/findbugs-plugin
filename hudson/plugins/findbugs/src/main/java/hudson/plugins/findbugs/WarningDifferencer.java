package hudson.plugins.findbugs;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides several utility methods based on sets of warnings.
 */
public final class WarningDifferencer {
    /**
     * Returns the new warnings, i.e., the warnings that are in the actual build
     * but not in the previous.
     *
     * @param actual
     *            warnings in actual build
     * @param previous
     *            warnings in previous build
     * @return the new warnings
     */
    public static Set<Warning> getNewWarnings(final Set<Warning> actual, final Set<Warning> previous) {
        Set<Warning> warnings = new HashSet<Warning>(actual);
        warnings.removeAll(previous);
        return warnings;
    }

    /**
     * Returns the fixed warnings, i.e., the warnings that are in the previous build
     * but not in the actial.
     *
     * @param actual
     *            warnings in actual build
     * @param previous
     *            warnings in previous build
     * @return the new warnings
     */
    public static Set<Warning> getFixedWarnings(final Set<Warning> actual, final Set<Warning> previous) {
        Set<Warning> warnings = new HashSet<Warning>(previous);
        warnings.removeAll(actual);
        return warnings;
    }

    /**
     * Returns the number of warnings with HIGH priority.
     *
     * @param warnings
     *            the warnings to scan
     * @return the number warnings of the specified priority.
     */
    public static int countHighPriorityWarnings(final Set<Warning> warnings) {
        return countWarnings(warnings, "high", "1");
    }

    /**
     * Returns the number of warnings of the specified priority.
     *
     * @param warnings
     *            the warnings to scan
     * @param priority
     *            the priority
     * @param priorityNumber
     *            the priority as a number
     * @return the number warnings of the specified priority.
     */
    private static int countWarnings(final Set<Warning> warnings, final String priority, final String priorityNumber) {
        int count = 0;
        for (Warning warning : warnings) {
            if (priority.equalsIgnoreCase(warning.getPriority())) {
                count++;
            }
            else if (priorityNumber.equalsIgnoreCase(warning.getPriority())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of warnings with NORMAL priority.
     *
     * @param warnings
     *            the warnings to scan
     * @return the number warnings of the specified priority.
     */
    public static int countNormalPriorityWarnings(final Set<Warning> warnings) {
        return countWarnings(warnings, "normal", "2");
    }

    /**
     * Returns the number of warnings with LOW priority.
     *
     * @param warnings
     *            the warnings to scan
     * @return the number warnings of the specified priority.
     */
    public static int countLowPriorityWarnings(final Set<Warning> warnings) {
        return countWarnings(warnings, "low", "3");
    }

    /**
     * Creates a new instance of <code>WarningDifferencer</code>.
     */
    private WarningDifferencer() {
        // prevents instantiation
    }
}

