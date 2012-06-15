package hudson.plugins.findbugs.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Kohsuke Kawaguchi
 */
public class HexishStringTest {
    @Test
    public void roundtrip() {
        verify("foo");
        verify("bar");
        verify("1234");
    }

    private void verify(String s) {
        assertEquals(s,new HexishString(s).toString());
    }
}
