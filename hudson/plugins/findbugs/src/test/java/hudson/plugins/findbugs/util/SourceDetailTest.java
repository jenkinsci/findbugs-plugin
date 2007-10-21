package hudson.plugins.findbugs.util;

import hudson.plugins.findbugs.Warning;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *  Tests the class {@link SourceDetail}.
 */
public class SourceDetailTest {
    /** Reference to line . */
    private static final String LINE_6_INDICATOR = "<a name=\"6\">";

    /**
     * Checks whether we correctly find a specific line in the generated source
     * code at a fixed line offset.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void checkCorrectOffset() throws IOException {
        Warning warning = new Warning();
        warning.setFile("file/path");
        warning.setLineNumberExpression("6");
        SourceDetail source = new SourceDetail(null, warning);

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
    }

    /**
     * Checks whether we correctly split the source into prefix, warning and suffix.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void testSplitting() throws IOException {
        InputStream stream = SourceDetailTest.class.getResourceAsStream("AbortException.txt");

        Warning warning = new Warning();
        warning.setFile("file/path");
        warning.setLineNumberExpression("6");
        SourceDetail source = new SourceDetail(null, warning);

        Assert.assertTrue("Prefix should not be empty.", StringUtils.isEmpty(source.getPrefix()));
        Assert.assertTrue("Suffix should not be empty.", StringUtils.isEmpty(source.getSuffix()));
        Assert.assertTrue(source.hasHighlightedLine());

        String highlighted = source.highlightSource(stream);
        source.splitSourceFile(highlighted);

        Assert.assertTrue("Wrong line selected as actual warning line.", source.getLine().contains(LINE_6_INDICATOR));
        Assert.assertFalse("Prefix should not be empty.", StringUtils.isEmpty(source.getPrefix()));
        Assert.assertFalse("Suffix should not be empty.", StringUtils.isEmpty(source.getSuffix()));
        Assert.assertTrue(source.hasHighlightedLine());
    }
}

