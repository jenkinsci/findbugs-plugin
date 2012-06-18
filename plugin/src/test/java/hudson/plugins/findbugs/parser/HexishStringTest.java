package hudson.plugins.findbugs.parser;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the class {@link HexishString}.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"PMD", "all"})
//CHECKSTYLE:OFF
public class HexishStringTest {
    @Test
    public void roundtrip() {
        verify("foo");
        verify("bar");
        verify("1234");
    }

    private void verify(final String s) {
        assertEquals(s,new HexishString(s).toString());
    }
}
