package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 *  Tests the extraction of findbugs analysis results.
 */
public class FindBugsCounterTest {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";
    /** Expected number of bugs. */
    private static final int NUMBER_OF_BUGS = 8;

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException {
        InputStream file = FindBugsCounterTest.class.getResourceAsStream("findbugs-no-errors.xml");
        Module module = new FindBugsCounter(null).parse(file);
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException {
        InputStream file = FindBugsCounterTest.class.getResourceAsStream("findbugs.xml");
        Module module = new FindBugsCounter(null).parse(file);
        assertEquals(ERROR_MESSAGE, NUMBER_OF_BUGS, module.getNumberOfWarnings());
        assertEquals("Wrong Version detected", "1.2.0", module.getVersion());
        assertEquals("Wrong number of packages detected", 2, module.getPackages().size());
    }
}


/* Copyright (c) Avaloq Evolution AG */