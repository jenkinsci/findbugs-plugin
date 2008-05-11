package hudson.plugins.findbugs;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import hudson.plugins.findbugs.util.AbstractEnglishLocaleTest;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest extends AbstractEnglishLocaleTest {
    /**
     * Checks the text for no warnings in 0 files.
     */
    @Test
    public void test0WarningsIn0File() {
        checkSummaryText(0, 0, "FindBugs: 0 warnings in 0 FindBugs files.");
    }

    /**
     * Checks the text for no warnings in 1 file.
     */
    @Test
    public void test0WarningsIn1File() {
        checkSummaryText(0, 1, "FindBugs: 0 warnings in 1 FindBugs file.");
    }

    /**
     * Checks the text for no warnings in 5 files.
     */
    @Test
    public void test0WarningsIn5Files() {
        checkSummaryText(0, 5, "FindBugs: 0 warnings in 5 FindBugs files.");
    }

    /**
     * Checks the text for 1 warning in 2 files.
     */
    @Test
    public void test1WarningIn2Files() {
        checkSummaryText(1, 2, "FindBugs: <a href=\"findbugsResult\">1 warning</a> in 2 FindBugs files.");
    }

    /**
     * Checks the text for 5 warnings in 1 file.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, 1, "FindBugs: <a href=\"findbugsResult\">5 warnings</a> in 1 FindBugs file.");
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
    private void checkSummaryText(final int numberOfWarnings, final int numberOfFiles, final String expectedMessage) {
        FindBugsResult result = createMock(FindBugsResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(numberOfWarnings).anyTimes();
        expect(result.getNumberOfModules()).andReturn(numberOfFiles).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong summary message created.", expectedMessage, ResultSummary.createSummary(result));

        verify(result);
    }

    /**
     * Checks the delta message for no new and no fixed warnings.
     */
    @Test
    public void testNoDelta() {
        checkDeltaText(0, 0, "");
    }

    /**
     * Checks the delta message for 1 new and no fixed warnings.
     */
    @Test
    public void testOnly1New() {
        checkDeltaText(0, 1, "<li><a href=\"findbugsResult/new\">1 new warning</a></li>");
    }

    /**
     * Checks the delta message for 5 new and no fixed warnings.
     */
    @Test
    public void testOnly5New() {
        checkDeltaText(0, 5, "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>");
    }

    /**
     * Checks the delta message for 1 fixed and no new warnings.
     */
    @Test
    public void testOnly1Fixed() {
        checkDeltaText(1, 0, "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and no new warnings.
     */
    @Test
    public void testOnly5Fixed() {
        checkDeltaText(5, 0, "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New5Fixed() {
        checkDeltaText(5, 5,
                "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test5New1Fixed() {
        checkDeltaText(1, 5,
        "<li><a href=\"findbugsResult/new\">5 new warnings</a></li>"
        + "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New5Fixed() {
        checkDeltaText(5, 1,
                "<li><a href=\"findbugsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new warnings.
     */
    @Test
    public void test1New1Fixed() {
        checkDeltaText(1, 1,
                "<li><a href=\"findbugsResult/new\">1 new warning</a></li>"
                + "<li><a href=\"findbugsResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfFixedWarnings
     *            the number of fixed warnings
     * @param numberOfNewWarnings
     *            the number of new warnings
     * @param expectedMessage
     *            the expected message
     */
    private void checkDeltaText(final int numberOfFixedWarnings, final int numberOfNewWarnings, final String expectedMessage) {
        FindBugsResult result = createMock(FindBugsResult.class);
        expect(result.getNumberOfFixedWarnings()).andReturn(numberOfFixedWarnings).anyTimes();
        expect(result.getNumberOfNewWarnings()).andReturn(numberOfNewWarnings).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong delta message created.", expectedMessage, ResultSummary.createDeltaMessage(result));

        verify(result);
    }
}

