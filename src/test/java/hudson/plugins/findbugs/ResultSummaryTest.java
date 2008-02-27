package hudson.plugins.findbugs;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest {
    /**
     * Checks the text for no warnings in 1 file.
     */
    @Test
    public void testNoWarningsInOneFile() {
        checkWarningText(0, 1, "FindBugs: 0 warnings in 1 FindBugs file.");
    }

    /**
     * Checks the text for no warnings in 1 file.
     */
    @Test
    public void testNoWarningsIn5Files() {
        checkWarningText(0, 5, "FindBugs: 0 warnings in 5 FindBugs files.");
    }

    /**
     * Checks the text for no warnings in 1 file.
     */
    @Test
    public void testOneWarningIn2Files() {
        checkWarningText(1, 2, "FindBugs: 1 warning in 2 FindBugs files.");
    }

    /**
     * Checks the text for no warnings in 1 file.
     */
    @Test
    public void test5WarningsInOneFile() {
        checkWarningText(5, 1, "FindBugs: 5 warnings in 1 FindBugs file.");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfWarnings
     *            the number of warnings
     * @param numberOfFiles
     *            the number of files
     * @param expectedMessage
     *            the expected message
     */
    private void checkWarningText(final int numberOfWarnings, final int numberOfFiles, final String expectedMessage) {
        FindBugsResult result = createMock(FindBugsResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(numberOfWarnings);
        expect(result.getNumberOfModules()).andReturn(numberOfFiles);

        replay(result);

        Assert.assertEquals(expectedMessage, ResultSummary.createSummary(result));

        verify(result);
    }

}

