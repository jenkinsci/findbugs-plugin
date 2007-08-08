package hudson.plugins.util;

import hudson.model.HealthReport;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests the class {@link HealthReportBuilder}.
 */
public class HealthReportBuilderTest extends TestCase {
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong healthiness calculation.";

    /**
     * Tests whether we evaluate correctly to a 50% health.
     */
    @Test
    public void testMiddle() {
        HealthReport health = createHelthReport(true, 50, 150, 100);
        assertEquals(ERROR_MESSAGE, 50, health.getScore());
    }

    /**
     * Tests whether we correctly display the result.
     */
    @Test
    public void testDisplay() {
        assertEquals(ERROR_MESSAGE, "FindBugs: 0 warnings found.", createHelthReport(true, 50, 150, 0).getDescription());
        assertEquals(ERROR_MESSAGE, "FindBugs: 1 warning found.", createHelthReport(true, 50, 150, 1).getDescription());
        assertEquals(ERROR_MESSAGE, "FindBugs: 2 warnings found.", createHelthReport(true, 50, 150, 2).getDescription());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health.
     */
    @Test
    public void testHigh() {
        HealthReport health = createHelthReport(true, 50, 150, 20);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health if lower than minimum.
     */
    @Test
    public void testHighBoundary() {
        HealthReport health = createHelthReport(true, 50, 150, 50);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health.
     */
    @Test
    public void testLow() {
        HealthReport health = createHelthReport(true, 50, 150, 200);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health if larger than maximum.
     */
    @Test
    public void testLowBoundary() {
        HealthReport health = createHelthReport(true, 50, 150, 150);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 25% health.
     */
    @Test
    public void test25Percent() {
        HealthReport health = createHelthReport(true, 0, 100, 75);
        assertEquals(ERROR_MESSAGE, 25, health.getScore());
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testNoHealthyReport() {
        HealthReport health = createHelthReport(false, 0, 100, 75);
        assertNull(ERROR_MESSAGE, health);
    }

    /**
     * Creates the test fixture.
     *
     * @param isEnabled
     *            defines whether health reporting is enabled
     * @param min
     *            minimum number of bugs
     * @param max
     *            maximum number of bugs
     * @param actual
     *            actual number of bugs
     * @return the actual healthiness
     */
    private HealthReport createHelthReport(final boolean isEnabled, final int min, final int max, final int actual) {
        HealthReportBuilder builder = new HealthReportBuilder("FindBugs", "warning", isEnabled, min, max);
        return builder.computeHealth(actual);
    }
}

