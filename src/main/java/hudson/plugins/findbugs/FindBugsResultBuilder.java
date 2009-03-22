package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.ParserResult;

/**
 * Creates a new FindBugs result based on the values of a previous build and the
 * current project.
 *
 * @author Ulli Hafner
 */
public class FindBugsResultBuilder {
    /**
     * Creates a result that persists the FindBugs information for the
     * specified build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the result action
     */
    public FindBugsResult build(final AbstractBuild<?, ?> build, final ParserResult project, final String defaultEncoding) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            FindBugsResultAction previousAction = previousBuild.getAction(FindBugsResultAction.class);
            if (previousAction != null) {
                return new FindBugsResult(build, defaultEncoding, project, previousAction.getResult());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new FindBugsResult(build, defaultEncoding, project);
    }

    /**
     * Creates a result that persists the FindBugs information for the
     * specified m2 build.
     *
     * @param build
     *            the build to create the action for
     * @param project
     *            the project containing the annotations
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @return the result action
     */
    public FindBugsMavenResult buildMaven(final AbstractBuild<?, ?> build, final ParserResult project, final String defaultEncoding) {
        Object previous = build.getPreviousBuild();
        while (previous instanceof AbstractBuild<?, ?>) {
            AbstractBuild<?, ?> previousBuild = (AbstractBuild<?, ?>)previous;
            FindBugsResultAction previousAction = previousBuild.getAction(FindBugsResultAction.class);
            if (previousAction != null) {
                return new FindBugsMavenResult(build, defaultEncoding, project, previousAction.getResult());
            }
            previous = previousBuild.getPreviousBuild();
        }
        return new FindBugsMavenResult(build, defaultEncoding, project);
    }
}

