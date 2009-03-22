package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.ParserResult;
import hudson.plugins.findbugs.util.model.JavaProject;

/**
 * Represents the aggregated results of the PMD analysis in m2 jobs.
 *
 * @author Ulli Hafner
 */
public class FindBugsMavenResult extends FindBugsResult {
    /** Unique ID of this class. */
    private static final long serialVersionUID = -4913938782537266259L;

    /**
     * Creates a new instance of {@link FindBugsMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    public FindBugsMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result) {
        super(build, defaultEncoding, result);
    }

    /**
     * Creates a new instance of {@link FindBugsMavenResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param previous
     *            the result of the previous build
     */
    public FindBugsMavenResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final FindBugsResult previous) {
        super(build, defaultEncoding, result, previous);
    }

    /**
     * Returns the results of the previous build.
     *
     * @return the result of the previous build, or <code>null</code> if no
     *         such build exists
     */
    @Override
    public JavaProject getPreviousResult() {
        MavenFindBugsResultAction action = getOwner().getAction(MavenFindBugsResultAction.class);
        if (action.hasPreviousResultAction()) {
            return action.getPreviousResultAction().getResult().getProject();
        }
        else {
            return null;
        }
    }

    /**
     * Returns whether a previous build result exists.
     *
     * @return <code>true</code> if a previous build result exists.
     */
    @Override
    public boolean hasPreviousResult() {
        return getOwner().getAction(MavenFindBugsResultAction.class).hasPreviousResultAction();
    }
}

