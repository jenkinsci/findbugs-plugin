package hudson.plugins.findbugs;

import org.junit.Assert;
import org.junit.Test;

/**
 *  Tests the class {@link Warning}.
 */
public class WarningTest {
    /**
     * Checks whether we correctly parse line number expressions.
     */
    @Test
    public void testSplitting() {
        Warning warning = new Warning();

        warning.setLineNumberExpression("6");
        Assert.assertEquals("Wrong line number", 6, warning.getLineNumber());

        warning.setLineNumberExpression("600-800");
        Assert.assertEquals("Wrong line number", 600, warning.getLineNumber());
    }
}

