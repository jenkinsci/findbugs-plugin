package hudson.plugins.findbugs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.maven.plugin.MojoExecution;
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
        assertTrue(WRONG_MAVEN_DETECTION, verify("2.4.0-SNAPSHOT"));
        assertTrue(WRONG_MAVEN_DETECTION, verify("2.4.0"));
        assertTrue(WRONG_MAVEN_DETECTION, verify("2.4.1"));
        assertTrue(WRONG_MAVEN_DETECTION, verify("2.5"));
        assertTrue(WRONG_MAVEN_DETECTION, verify("3.0.1"));
        assertTrue(WRONG_MAVEN_DETECTION, verify("3.0"));

        assertFalse(WRONG_MAVEN_DETECTION, verify("2.3.4"));
        assertFalse(WRONG_MAVEN_DETECTION, verify("1.3.4"));

        assertFalse(WRONG_MAVEN_DETECTION, verify("2"));
        assertFalse(WRONG_MAVEN_DETECTION, verify("Nothing"));
    }

    private boolean verify(final String version) {
        MojoExecution execution = mock(MojoExecution.class);
        when(execution.getVersion()).thenReturn(version);
        return FindBugsPlugin.isFindBugs2x(execution);
    }

    /**
     * Verifies that exceptions during version detection are catched.
     */
    @Test
    public void testNoSuchMethodError() {
        MojoExecution execution = mock(MojoExecution.class);
        when(execution.getVersion()).thenThrow(new NoSuchMethodError());

        assertFalse(WRONG_MAVEN_DETECTION, FindBugsPlugin.isFindBugs2x(execution));
    }
}

