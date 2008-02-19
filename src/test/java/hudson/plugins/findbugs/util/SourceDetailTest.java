package hudson.plugins.findbugs.util;

import static org.easymock.EasyMock.*;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.LineRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  Tests the class {@link SourceDetail}.
 */
public class SourceDetailTest {
    /** Start of the range. */
    private static final int START = 6;
    /** Reference to line 6. */
    private static final String LINE_6_INDICATOR = "<a name=\"" + START + "\">";

    /**
     * Checks whether we correctly find a specific line in the generated source
     * code at a fixed line offset.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void checkCorrectOffset() throws IOException {
        FileAnnotation annotation = createMock(FileAnnotation.class);

        expect(annotation.getWorkspaceFileName()).andReturn("").anyTimes();

        replay(annotation);

        SourceDetail source = new SourceDetail(null, annotation);

        InputStream stream = SourceDetailTest.class.getResourceAsStream("AbortException.txt");
        String highlighted = source.highlightSource(stream);

        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(highlighted));

        int line = 1;
        int offset = 1;
        while (lineIterator.hasNext()) {
            String content = lineIterator.nextLine();
            if (content.contains(LINE_6_INDICATOR)) {
                offset  = line - 6;
            }
            line++;
        }
        Assert.assertEquals("Wrong offset during source highlighting.", 12, offset);

        verify(annotation);
    }

    /**
     * Checks whether we correctly split the source into prefix, warning and suffix for a single line.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void splitSingleLine() throws IOException {
        split(START);
    }

    /**
     * Checks whether we correctly split the source into prefix, warning and suffix for a range of 4 lines.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void splitLineRange() throws IOException {
        split(10);
    }

    /**
     * Checks whether we correctly split the source into prefix, warning and
     * suffix.
     *
     * @param end
     *            last line of the range
     * @throws IOException
     *             in case of an IO error
     */
    private void split(final int end) throws IOException {
        InputStream stream = SourceDetailTest.class.getResourceAsStream("AbortException.txt");

        FileAnnotation annotation = createMock(FileAnnotation.class);
        LineRange lineRange = new LineRange(6, end);

        ArrayList<LineRange> lineRanges = new ArrayList<LineRange>();
        lineRanges.add(lineRange);

        expect(annotation.getLineRanges()).andReturn(lineRanges);
        expect(annotation.getWorkspaceFileName()).andReturn("").anyTimes();

        replay(annotation);

        SourceDetail source = new SourceDetail(null, annotation);

        Assert.assertTrue("Prefix should be empty.", StringUtils.isEmpty(source.getPrefix()));
        Assert.assertTrue("Suffix should be empty.", StringUtils.isEmpty(source.getSuffix()));
        Assert.assertTrue(source.hasHighlightedLine());

        String highlighted = source.highlightSource(stream);
        source.splitSourceFile(highlighted);

        Assert.assertTrue("Wrong line selected as actual warning line.", source.getLine().contains(LINE_6_INDICATOR));
        Assert.assertFalse("Prefix should not be empty.", StringUtils.isEmpty(source.getPrefix()));
        Assert.assertFalse("Suffix should not be empty.", StringUtils.isEmpty(source.getSuffix()));
        Assert.assertTrue(source.hasHighlightedLine());

        Assert.assertEquals(end - 6 + 1, IOUtils.readLines(new StringReader(source.getLine())).size());

        verify(annotation);
    }
}

