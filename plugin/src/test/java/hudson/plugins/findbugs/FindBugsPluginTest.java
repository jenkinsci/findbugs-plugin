package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link FindBugsPlugin}.
 *
 * @author Ulli Hafner
 */
public class FindBugsPluginTest {
    private static final String WRONG_MAVEN_DETECTION = "Wrong maven detection";

    /**
     * Verifies mapping of maven versions to FindBugs versions.
     */
    @Test
    public void testFindBugsVersion() {
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2.4.0-SNAPSHOT"));
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2.4.0"));
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2.4.1"));
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2.5"));
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("3.0.1"));
        assertTrue(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("3.0"));

        assertFalse(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2.3.4"));
        assertFalse(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("1.3.4"));

        assertFalse(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("2"));
        assertFalse(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x("Nothing"));
    }
}

