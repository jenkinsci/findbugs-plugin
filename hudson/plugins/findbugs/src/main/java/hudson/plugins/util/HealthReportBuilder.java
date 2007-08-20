package hudson.plugins.util;

import java.io.Serializable;

import hudson.Util;
import hudson.model.HealthReport;

/**
 * Creates a health report for integer values based on healthy and unhealthy
 * thresholds.
 *
 * @see HealthReport
 */
public class HealthReportBuilder implements Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 5191317904662711835L;
    /** Report health as 100% when the number of warnings is less than this value. */
    private int healthy;
    /** Report health as 0% when the number of warnings is greater than this value. */
    private int unHealthy;
    /** Determines whether to use the provided healthy thresholds. */
    private boolean isEnabled;
    /** Name of the report. */
    private String reportName;
    /** Name of a item. */
    private String itemName;

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
        this.healthy = healthy;
        this.unHealthy = unHealthy;
        isEnabled = isHealthyReportEnabled;
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
        if (isEnabled) {
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

    /**
     * Returns the healthy.
     *
     * @return the healthy
     */
    public final int getHealthy() {
        return healthy;
    }

    /**
     * Sets the healthy to the specified value.
     *
     * @param healthy the value to set
     */
    public final void setHealthy(final int healthy) {
        this.healthy = healthy;
    }

    /**
     * Returns the unHealthy.
     *
     * @return the unHealthy
     */
    public final int getUnHealthy() {
        return unHealthy;
    }

    /**
     * Sets the unHealthy to the specified value.
     *
     * @param unHealthy the value to set
     */
    public final void setUnHealthy(final int unHealthy) {
        this.unHealthy = unHealthy;
    }

    /**
     * Returns the isHealthyReportEnabled.
     *
     * @return the isHealthyReportEnabled
     */
    public final boolean isHealthyReportEnabled() {
        return isEnabled;
    }

    /**
     * Sets the isHealthyReportEnabled to the specified value.
     *
     * @param isHealthyReportEnabled the value to set
     */
    public final void setHealthyReportEnabled(final boolean isHealthyReportEnabled) {
        isEnabled = isHealthyReportEnabled;
    }

    /**
     * Returns the reportName.
     *
     * @return the reportName
     */
    public final String getReportName() {
        return reportName;
    }

    /**
     * Sets the reportName to the specified value.
     *
     * @param reportName the value to set
     */
    public final void setReportName(final String reportName) {
        this.reportName = reportName;
    }

    /**
     * Returns the itemName.
     *
     * @return the itemName
     */
    public final String getItemName() {
        return itemName;
    }

    /**
     * Sets the itemName to the specified value.
     *
     * @param itemName the value to set
     */
    public final void setItemName(final String itemName) {
        this.itemName = itemName;
    }
}

