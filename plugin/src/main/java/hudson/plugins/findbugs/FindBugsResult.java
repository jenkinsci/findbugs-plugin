package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.findbugs.parser.Bug;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class FindBugsResult extends BuildResult {
    private static final long serialVersionUID = 2768250056765266658L;

    private static final int LESS_ONE_WEEK = 6;

    private int newThisWeek;
    private int notInCloud = 0;
    private int numberOfComments;

    /**
     * Creates a new instance of {@link FindBugsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    public FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result) {
        super(build, defaultEncoding, result);
        init();
    }

    /**
     * Creates a new instance of {@link FindBugsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param history
     *            the history of build results of the associated plug-in
     */
    public FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final BuildHistory history) {
        super(build, defaultEncoding, result, history);
        init();
    }

    private void init() {
        for (FileAnnotation annotation : getAnnotations()) {
            if (annotation instanceof Bug) {
                Bug bug = (Bug) annotation;
                if (bug.isInCloud()) {
                    if (bug.getAgeInDays() <= LESS_ONE_WEEK) {
                        newThisWeek++;
                    }
                    numberOfComments += bug.getReviewCount();
                } else {
                    notInCloud++;
                }
            }
        }
    }

    /**
     * Gets the number of reviewer comments for all bugs.
     *
     * @return the number of comments
     */
    public int getNumberOfComments() {
        return numberOfComments;
    }

    /** {@inheritDoc} */
    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("bug", Bug.class);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /**
     * Gets the number of new bugs new this week.
     *
     * @return the number of new bugs this week
     */
    public int getNewThisWeek() {
        return newThisWeek;
    }

    /**
     * Gets the number of bugs which are not stored in the FindBugs Cloud.
     *
     * @return the number of bugs which are not stored in the FindBugs Cloud
     */
    public int getNotInCloud() {
        return notInCloud;
    }

    /** {@inheritDoc} */
    @Override
    protected String createDeltaMessage() {
        return ResultSummary.createDeltaMessage(this);
    }

    /**
     * Returns the name of the file to store the serialized annotations.
     *
     * @return the name of the file to store the serialized annotations
     */
    @Override
    protected String getSerializationFileName() {
        return "findbugs-warnings.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return FindBugsResultAction.class;
    }
}
