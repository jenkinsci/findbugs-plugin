package hudson.plugins.findbugs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Assert;
import org.junit.Test;

/**
 *  Tests the class {@link FindBugsSource}.
 */
public class FindBugsSourceTest {
    /** Reference to line . */
    private static final String LINE_6_INDICATOR = "<a name=\"6\">";

    /**
     * Checks whether we correctly find a specific line in the generated souce
     * code at a fixed line offset.
     *
     * @throws IOException in case of an IO error
     */
    @Test
    public void testHighlighting() throws IOException {
        InputStream stream = FindBugsSourceTest.class.getResourceAsStream("AbortException.txt");

        Warning warning = new Warning();
        warning.setFile("file/path");
        FindBugsSource source = new FindBugsSource(null, warning);

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

}

