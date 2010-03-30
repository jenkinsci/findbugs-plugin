package hudson.plugins.findbugs;

/**
 * Bug pattern describing a bug type.
 *
 * @author Ulli Hafner
 */
public class Pattern {
    /** Type of the bug. */
    private String type;
    /** Detailed HTML description of the bug. */
    private String description;
    /** Short description of the bug. */
    private String shortDescription;

    /**
     * Sets the type to the specified value.
     *
     * @param type the value to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the description to the specified value.
     *
     * @param description the value to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the shortDescription to the specified value.
     *
     * @param shortDescription the value to set
     */
    public void setShortDescription(final String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * Returns the shortDescription.
     *
     * @return the shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }
}

