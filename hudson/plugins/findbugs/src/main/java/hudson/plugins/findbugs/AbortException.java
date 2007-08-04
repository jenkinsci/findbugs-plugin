package hudson.plugins.findbugs;

/**
 * Indicates an orderly abortion of the processing.
 */
final class AbortException extends RuntimeException {
    /** Generated ID. */
    private static final long serialVersionUID = -5897876033901702893L;

    /**
     * Instantiates a new abort exception.
     *
     * @param message the exception message
     */
    AbortException(final String message) {
        super(message);
    }
}