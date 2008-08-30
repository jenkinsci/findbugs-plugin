package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.util.AnnotationDifferencer;
import hudson.plugins.findbugs.util.AnnotationDifferencerTest;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.Priority;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Tests the {@link AnnotationDifferencer} for bugs.
 */
public class BugsDifferencerTest extends AnnotationDifferencerTest {
    /** {@inheritDoc} */
    @Override
    public FileAnnotation createAnnotation(final String fileName, final Priority priority, final String message, final String category,
            final String type, final int start, final int end) {
        Bug bug = new Bug(priority, message, message, message, start, end);
        bug.setFileName(fileName);
        bug.setInstanceHash(String.valueOf(new HashCodeBuilder().append(priority).append(message).append(category).append(type).append(start).append(end).toHashCode()));
        return bug;
    }
}

