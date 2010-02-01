package hudson.plugins.findbugs.parser.maven;

import org.junit.Assert;
import org.junit.Test;


/**
 *  Tests the class {@link BugInstance}.
 */
public class BugInstanceTest {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong line number";

    /**
     * Checks whether we correctly parse line number expressions "X".
     */
    @Test
    public void testSimpleLineAssignment() {
        BugInstance warning = new BugInstance();

        warning.setLineNumberExpression("6");
        Assert.assertEquals(ERROR_MESSAGE, 6, warning.getStart());
        Assert.assertEquals(ERROR_MESSAGE, 6, warning.getEnd());
    }

    /**
     * Checks whether we correctly parse line number expressions "X-Y".
     */
    @Test
    public void testLineRangeAssignment() {
        BugInstance warning = new BugInstance();

        warning.setLineNumberExpression("600-800");
        Assert.assertEquals(ERROR_MESSAGE, 600, warning.getStart());
        Assert.assertEquals(ERROR_MESSAGE, 800, warning.getEnd());
    }


    /**
     * Checks whether we correctly parse line number expressions "Not available".
     */
    @Test
    public void testNoRangeAssignment() {
        BugInstance warning = new BugInstance();

        warning.setLineNumberExpression("Not available");
        Assert.assertEquals(ERROR_MESSAGE, 0, warning.getStart());
        Assert.assertEquals(ERROR_MESSAGE, 0, warning.getEnd());
    }
}

