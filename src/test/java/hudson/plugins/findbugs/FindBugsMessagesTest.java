package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

/**
 * Tests whether we could parse the FindBugs warning files.
 */
public class FindBugsMessagesTest {
    /** Expected number of patterns. */
    private static final int EXPECTED_PATTERNS = 309;

    /**
     * Checks the number of different FindBugs messages.
     *
     * @throws Exception if we can't read the file
     */
    @Test
    public void parseFindbugsMessages() throws Exception {
        InputStream file = FindBugsCounterTest.class.getResourceAsStream("messages.xml");
        List<Pattern> patterns = FindBugsMessages.getInstance().parse(file);

        assertEquals("Wrong number of warnings detected.", EXPECTED_PATTERNS, patterns.size());
    }
}

