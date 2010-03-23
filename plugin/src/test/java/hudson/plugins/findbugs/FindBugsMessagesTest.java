package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Tests whether we could parse the FindBugs warning files.
 */
public class FindBugsMessagesTest {
    /** Bug ID for test. */
    private static final String NP_STORE_INTO_NONNULL_FIELD = "NP_STORE_INTO_NONNULL_FIELD";
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_DETECTED = "Wrong number of warnings detected.";
    /** Error message. */
    private static final String WRONG_WARNING_MESSAGE = "Wrong warning message.";
    /** Expected number of patterns. */
    private static final int EXPECTED_PATTERNS = 405;
    /** Expected number of patterns in fb-contrib. */
    private static final int EXPECTED_CONTRIB_PATTERNS = 113;

    /**
     * Checks the number of different FindBugs messages.
     *
     * @throws SAXException
     *             if we can't read the file
     * @throws IOException
     *             if we can't read the file
     */
    @Test
    public void parseFindbugsMessages() throws IOException, SAXException {
        InputStream file = FindBugsMessages.class.getResourceAsStream("messages.xml");
        List<Pattern> patterns = FindBugsMessages.getInstance().parse(file);

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, EXPECTED_PATTERNS, patterns.size());
    }

    /**
     * Checks the number of different FindBugs messages in the fb-contrib package.
     *
     * @throws SAXException
     *             if we can't read the file
     * @throws IOException
     *             if we can't read the file
     */
    @Test
    public void parseFindbugsContribMessages() throws IOException, SAXException {
        InputStream file = FindBugsMessages.class.getResourceAsStream("fb-contrib-messages.xml");
        List<Pattern> patterns = FindBugsMessages.getInstance().parse(file);

        assertEquals(WRONG_NUMBER_OF_WARNINGS_DETECTED, EXPECTED_CONTRIB_PATTERNS, patterns.size());
    }

    /**
     * Checks that a warning message of each file is correctly parsed.
     *
     * @throws SAXException
     *             if we can't read the file
     * @throws IOException
     *             if we can't read the file
     */
    @Test
    public void parse() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();

        assertTrue(WRONG_WARNING_MESSAGE, FindBugsMessages.getInstance().getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.ENGLISH).contains("A value that could be null is stored into a field that has been annotated as NonNull."));
        assertTrue(WRONG_WARNING_MESSAGE, FindBugsMessages.getInstance().getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.GERMAN).contains("A value that could be null is stored into a field that has been annotated as NonNull."));
        assertEquals(WRONG_WARNING_MESSAGE, "Store of null value into field annotated NonNull", FindBugsMessages.getInstance().getShortMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.ENGLISH));
        assertTrue(WRONG_WARNING_MESSAGE, FindBugsMessages.getInstance().getMessage("NMCS_NEEDLESS_MEMBER_COLLECTION_SYNCHRONIZATION", Locale.ENGLISH).contains("This class defines a private collection member as synchronized. It appears however"));
        assertEquals(WRONG_WARNING_MESSAGE, "Class defines unneeded synchronization on member collection", FindBugsMessages.getInstance().getShortMessage("NMCS_NEEDLESS_MEMBER_COLLECTION_SYNCHRONIZATION", Locale.ENGLISH));
    }

    /**
     * Checks that localized messages are loaded.
     *
     * @throws SAXException
     *             if we can't read the file
     * @throws IOException
     *             if we can't read the file
     */
    @Test
    public void parseLocalizations() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();

        assertTrue(WRONG_WARNING_MESSAGE, FindBugsMessages.getInstance().getShortMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.FRANCE).contains("Stocke une valeur null dans"));
        assertTrue(WRONG_WARNING_MESSAGE, FindBugsMessages.getInstance().getMessage(NP_STORE_INTO_NONNULL_FIELD, Locale.FRANCE).contains("Une valeur qui pourrait"));
    }
}

