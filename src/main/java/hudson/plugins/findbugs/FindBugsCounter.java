package hudson.plugins.findbugs;

import org.apache.commons.io.LineIterator;

/**
 * Counts the number of bugs in a FindBugs analysis file.
 *
 * <p>
 * TODO: extract all FindBugs information instead of just counting
 */
public class FindBugsCounter {
    /**
     * Returns the number of bugs in the FindBugs analysis file.
     *
     * @param lines the lines of the FindBugs analysis file
     * @return the number of bugs
     */
    public int count(final LineIterator lines) {
        int bugs = 0;
        while (lines.hasNext()) {
            String line = lines.nextLine();
            if (line.contains("<BugInstance ")) {
                bugs++;
            }
        }
        return bugs;
    }
}