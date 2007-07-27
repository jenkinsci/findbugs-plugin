package hudson.plugins.findbugs;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;


/**
 *  Tests the extraction of findbugs analysis results.
 */
public class FindBugsExtractorTest {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";
    /** Expected number of bugs. */
    private static final int NUMBER_OF_BUGS = 8;

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException {
        InputStream file = FindBugsExtractorTest.class.getClassLoader().getResourceAsStream("findbugs.xml");
        assertEquals(ERROR_MESSAGE, NUMBER_OF_BUGS, new FindBugsCounter().count(readLines(file)));
    }

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException {
        InputStream file = FindBugsExtractorTest.class.getClassLoader().getResourceAsStream("findbugs-no-errors.xml");
        assertEquals(ERROR_MESSAGE, 0, new FindBugsCounter().count(readLines(file)));
    }

    /**
     * Reads the lines of the specified file.
     *
     * @param file
     *            the file to read the lines from
     * @return the lines
     * @throws IOException
     *             in case of an IO error
     */
    private LineIterator readLines(final InputStream file) throws IOException {
        return IOUtils.lineIterator(file, "UTF-8");
    }
}


/* Copyright (c) Avaloq Evolution AG */