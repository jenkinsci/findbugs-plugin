package hudson.plugins.findbugs;

import hudson.Util;

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
        return "FindBugs: "
            + Util.combine(result.getNumberOfAnnotations(), "warning")
            +  " in "
            + Util.combine(result.getNumberOfModules(), "FindBugs file")
            + ".";
    }

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

