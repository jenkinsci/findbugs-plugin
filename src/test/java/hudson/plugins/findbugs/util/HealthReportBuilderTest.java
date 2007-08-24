package hudson.plugins.findbugs.util;

import hudson.model.HealthReport;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests the class {@link HealthReportBuilder}.
 */
public class HealthReportBuilderTest extends TestCase {
    /** Item name. */
    private static final String WARNING = "warning";
    /** Header. */
    private static final String FIND_BUGS = "FindBugs";
    /** Error message. */
    private static final String WRONG_SERIES_VALUE = "Wrong serieass value.";
    /** Error message. */
    private static final String WRONG_NUMBER = "Number of created point is wrong.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong healthiness calculation.";

    /**
     * Tests whether we evaluate correctly to a 50% health.
     */
    @Test
    public void testMiddle() {
        HealthReport health = createHealthReport(true, 50, 150, 100);
        assertEquals(ERROR_MESSAGE, 50, health.getScore());
    }

    /**
     * Tests whether we correctly display the result.
     */
    @Test
    public void testDisplay() {
        assertEquals(ERROR_MESSAGE, "FindBugs: 0 warnings found.", createHealthReport(true, 50, 150, 0).getDescription());
        assertEquals(ERROR_MESSAGE, "FindBugs: 1 warning found.", createHealthReport(true, 50, 150, 1).getDescription());
        assertEquals(ERROR_MESSAGE, "FindBugs: 2 warnings found.", createHealthReport(true, 50, 150, 2).getDescription());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health.
     */
    @Test
    public void testHigh() {
        HealthReport health = createHealthReport(true, 50, 150, 20);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 100% health if lower than minimum.
     */
    @Test
    public void testHighBoundary() {
        HealthReport health = createHealthReport(true, 50, 150, 50);
        assertEquals(ERROR_MESSAGE, 100, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health.
     */
    @Test
    public void testLow() {
        HealthReport health = createHealthReport(true, 50, 150, 200);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 0% health if larger than maximum.
     */
    @Test
    public void testLowBoundary() {
        HealthReport health = createHealthReport(true, 50, 150, 150);
        assertEquals(ERROR_MESSAGE, 0, health.getScore());
    }

    /**
     * Tests whether we evaluate correctly to a 25% health.
     */
    @Test
    public void test25Percent() {
        HealthReport health = createHealthReport(true, 0, 100, 75);
        assertEquals(ERROR_MESSAGE, 25, health.getScore());
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testNoHealthyReport() {
        HealthReport health = createHealthReport(false, 0, 100, 75);
        assertNull(ERROR_MESSAGE, health);
    }

    /**
     * Tests whether we correctly compute the series if health reporting is enabled.
     */
    @Test
    public void testHealthySeriesCalculator() {
        HealthReportBuilder builder = new HealthReportBuilder(FIND_BUGS, WARNING, true, 0, true, 10, 30);

        List<Integer> series = builder.createSeries(5);
        assertEquals(WRONG_NUMBER, 1, series.size());
        assertEquals(WRONG_SERIES_VALUE, 5, (int)series.get(0));

        series = builder.createSeries(10);
        assertEquals(WRONG_NUMBER, 1, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));

        series = builder.createSeries(11);
        assertEquals(WRONG_NUMBER, 2, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(1));

        series = builder.createSeries(30);
        assertEquals(WRONG_NUMBER, 2, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 20, (int)series.get(1));

        series = builder.createSeries(31);
        assertEquals(WRONG_NUMBER, 3, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 20, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(2));
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testThresholdSeriesCalculator() {
        HealthReportBuilder builder = new HealthReportBuilder(FIND_BUGS, WARNING, true, 10, false, 20, 50);

        List<Integer> series = builder.createSeries(5);
        assertEquals(WRONG_NUMBER, 1, series.size());
        assertEquals(WRONG_SERIES_VALUE, 5, (int)series.get(0));

        series = builder.createSeries(10);
        assertEquals(WRONG_NUMBER, 1, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));

        series = builder.createSeries(11);
        assertEquals(WRONG_NUMBER, 2, series.size());
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(0));
        assertEquals(WRONG_SERIES_VALUE, 1, (int)series.get(1));
    }

    /**
     * Tests whether we don't get a healthy report if the reporting is disabled.
     */
    @Test
    public void testSimpleSeriesCalculator() {
        HealthReportBuilder builder = new HealthReportBuilder(FIND_BUGS, WARNING, false, 10, false, 10, 30);

        List<Integer> series = builder.createSeries(5, 10, 15);
        assertEquals(WRONG_NUMBER, 3, series.size());
        assertEquals(WRONG_SERIES_VALUE, 5, (int)series.get(2));
        assertEquals(WRONG_SERIES_VALUE, 10, (int)series.get(1));
        assertEquals(WRONG_SERIES_VALUE, 15, (int)series.get(0));
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
    private HealthReport createHealthReport(final boolean isEnabled, final int min, final int max, final int actual) {
        HealthReportBuilder builder = new HealthReportBuilder(FIND_BUGS, WARNING, false, 0, isEnabled, min, max);
        return builder.computeHealth(actual);
    }
}

