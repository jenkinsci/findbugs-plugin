package hudson.plugins.findbugs.parser;

/**
 * Java Bean to create the mapping of hash codes to messages using the Digester
 * XML parser.
 *
 * @author Ulli Hafner
 */
public class XmlBugInstance {
    /** Unique ID of a bug. */
    private String instanceHash;
    private String message;
    private String type;
    private String category;

    /**
     * Returns the hash code of this bug.
     *
     * @return the hash code
     */
    public String getInstanceHash() {
        return instanceHash;
    }

    /**
     * Sets the hash to the specified value.
     *
     * @param instanceHash
     *            the value to set
     */
    public void setInstanceHash(final String instanceHash) {
        this.instanceHash = instanceHash;
    }

    /**
     * Returns the message for this bug.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message to the specified value.
     *
     * @param message
     *            the value to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
