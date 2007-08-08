package hudson.plugins.util;

import hudson.Util;
import hudson.model.HealthReport;

/**
 * Creates a health report for integer values based on healthy and unhealthy
 * thresholds.
 *
 * @see HealthReport
 */
public class HealthReportBuilder {
    /** Report health as 100% when the number of warnings is less than this value. */
    private final int healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private final int unHealthy;
    /** Determines whether to use the provided healthy thresholds. */
    private final boolean isHealthyReportEnabled;
    /** Name of the report. */
    private final String reportName;
    /** Name of a item. */
    private final String itemName;

    /**
     * Creates a new instance of <code>HealthReportBuilder</code>.
     *
     * @param reportName
     *            the report name
     * @param itemName
     *            the item name
     * @param isHealthyReportEnabled
     *            Determines whether to use the provided healthy thresholds.
     * @param healthy
     *            Report health as 100% when the number of warnings is less than
     *            this value
     * @param unHealthy
     *            Report health as 0% when the number of warnings is greater
     *            than this value
     */
    public HealthReportBuilder(final String reportName, final String itemName, final boolean isHealthyReportEnabled, final int healthy, final int unHealthy) {
        this.reportName = reportName;
        this.itemName = itemName;
        this.isHealthyReportEnabled = isHealthyReportEnabled;
        this.healthy = healthy;
        this.unHealthy = unHealthy;
    }

    /**
     * Computes the healthiness of a build based on the specified counter.
     * Reports a health of 100% when the specified counter is less than
     * {@link #healthy}. Reports a health of 0% when the specified counter is
     * greater than {@link #unHealthy}.
     *
     * @param counter
     *            the number of items in a build
     * @return the healthiness of a build
     */
    public HealthReport computeHealth(final int counter) {
        if (isHealthyReportEnabled) {
            int percentage;
            if (counter < healthy) {
                percentage = 100;
            }
            else if (counter > unHealthy) {
                percentage = 0;
            }
            else {
                percentage = 100 - ((counter - healthy) * 100 / (unHealthy - healthy));
            }
            return new HealthReport(percentage,
                    reportName + ": " + Util.combine(counter, itemName) + " found.");
        }
        else {
            return null;
        }
    }
}

