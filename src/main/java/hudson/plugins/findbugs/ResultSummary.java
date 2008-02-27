package hudson.plugins.findbugs;

/**
 * Represents the result summary of the FindBugs parser. This summary will be
 * shown in the summary.jelly script of the FindBugs result action.
 */
public class ResultSummary {
    /** The message to show. */
    private String message;

    /**
     * Creates a new instance of <code>ResultSummary</code>.
     *
     * @param result
     *            the FindBugs parser result
     */
    public ResultSummary(final FindBugsResult result) {
        if (result.getNumberOfAnnotations() == 0) {
            message = String.format("FindBugs: no warnings have been found in %d FindBugs files.", result.getNumberOfModules());
        }
    }

    /**
     * Returns the message to show as the result summary.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}

