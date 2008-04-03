package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.util.AnnotationDifferencer;
import hudson.plugins.findbugs.util.AnnotationDifferencerTest;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.Priority;

/**
 * Tests the {@link AnnotationDifferencer} for bugs.
 */
public class BugsDifferencerTest extends AnnotationDifferencerTest {
    /** {@inheritDoc} */
    @Override
    public FileAnnotation createAnnotation(final Priority priority, final String message, final String category,
            final String type, final int start, final int end) {
        return new Bug(priority, message, message, message, start, end);
    }
}

