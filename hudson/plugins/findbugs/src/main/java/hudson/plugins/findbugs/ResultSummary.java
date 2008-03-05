package hudson.plugins.findbugs;


/**
 * Represents the result summary of the FindBugs parser. This summary will be
 * shown in the summary.jelly script of the FindBugs result action.
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createSummary(final FindBugsResult result) {
        StringBuilder summary = new StringBuilder();
        int bugs = result.getNumberOfAnnotations();

        summary.append("FindBugs: ");
        if (bugs > 0) {
            summary.append("<a href=\"findbugsResult\">");
        }
        if (bugs == 1) {
            summary.append("1 warning");
        }
        else {
            summary.append(bugs + " warnings");
        }
        if (bugs > 0) {
            summary.append("</a>");
        }
        summary.append(" ");
        if (result.getNumberOfModules() > 1) {
            summary.append("in " + result.getNumberOfModules() + " FindBugs files.");
        }
        else {
            summary.append("in 1 FindBugs file.");
        }
        return summary.toString();
    }

    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createDeltaMessage(final FindBugsResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"findbugsResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append("1 new warning");
            }
            else {
                summary.append(result.getNumberOfNewWarnings() + " new warnings");
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"findbugsResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append("1 fixed warning");
            }
            else {
                summary.append(result.getNumberOfFixedWarnings() + " fixed warnings");
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

