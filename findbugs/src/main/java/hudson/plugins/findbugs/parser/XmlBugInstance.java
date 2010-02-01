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
    /** Message describing the bug. */
    private String message;

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
}

