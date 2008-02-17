package hudson.plugins.findbugs;

import hudson.plugins.findbugs.model.FileAnnotation;

import java.util.Collection;
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
    public static Set<FileAnnotation> getNewWarnings(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        Set<FileAnnotation> warnings = new HashSet<FileAnnotation>(actual);
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
    public static Set<FileAnnotation> getFixedWarnings(final Collection<FileAnnotation> actual, final Collection<FileAnnotation> previous) {
        Set<FileAnnotation> warnings = new HashSet<FileAnnotation>(previous);
        warnings.removeAll(actual);
        return warnings;
    }

    /**
     * Creates a new instance of <code>WarningDifferencer</code>.
     */
    private WarningDifferencer() {
        // prevents instantiation
    }
}

