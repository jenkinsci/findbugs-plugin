package hudson.plugins.findbugs;

/**
 * Provides a warnings count for an object.
 */
public interface WarningProvider {
    /**
     * Returns the total number of warnings.
     *
     * @return the total number of warnings
     */
    int getNumberOfWarnings();

    /**
     * Returns the total number of warnings with priority LOW.
     *
     * @return the total number of warnings with priority LOW
     */
    int getNumberOfLowWarnings();

    /**
     * Returns the total number of warnings with priority HIGH.
     *
     * @return the total number of warnings with priority HIGH
     */
    int getNumberOfHighWarnings();

    /**
     * Returns the total number of warnings with priority NORMAL.
     *
     * @return the total number of warnings with priority NORMAL
     */
    int getNumberOfNormalWarnings();
}
