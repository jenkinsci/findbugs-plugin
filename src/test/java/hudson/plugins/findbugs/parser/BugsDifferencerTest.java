package hudson.plugins.findbugs.parser;

import static junit.framework.Assert.*;
import hudson.plugins.analysis.core.AnnotationDifferencer;
import hudson.plugins.analysis.test.AnnotationDifferencerTest;
import hudson.plugins.analysis.util.model.AnnotationStream;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.Priority;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Test;

import com.google.common.collect.Sets;

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

    /**
     * Tests whether the instance hash of the findbugs library is correctly used.
     */
    @Test
    public void testInstanceHash() {
        AnnotationStream xstream = new AnnotationStream();
        xstream.alias("bug", Bug.class);

        FileAnnotation[] current = (FileAnnotation[])xstream.fromXML(BugsDifferencerTest.class.getResourceAsStream("issue-6669-1.xml"));
        assertEquals("Wrong number of bugs", 2, current.length);

        FileAnnotation[] previous = (FileAnnotation[])xstream.fromXML(BugsDifferencerTest.class.getResourceAsStream("issue-6669-2.xml"));
        assertEquals("Wrong number of bugs", 2, previous.length);

        HashSet<FileAnnotation> currentSet = Sets.newHashSet(Arrays.asList(current));
        HashSet<FileAnnotation> previousSet = Sets.newHashSet(Arrays.asList(previous));
        assertEquals("Wrong number of fixed bugs", 0,
                AnnotationDifferencer.getFixedAnnotations(currentSet, previousSet).size());
        assertEquals("Wrong number of new bugs", 0,
                AnnotationDifferencer.getNewAnnotations(currentSet, previousSet).size());
    }
}

