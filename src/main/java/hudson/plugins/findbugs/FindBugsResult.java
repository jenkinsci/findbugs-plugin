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

    private int newThisWeek = 0;
    private int numberOfComments = 0;

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
        int newThisWeek = 0;
        int reviewCount = 0;
        for (FileAnnotation annotation : getAnnotations()) {
            if (annotation instanceof Bug) {
                Bug bug = (Bug) annotation;
                if (bug.getAgeInDays() <= 6)
                    newThisWeek++;
                reviewCount += bug.getReviewCount();
            }
        }
        this.newThisWeek = newThisWeek;
        this.numberOfComments = reviewCount;
    }

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

    public int getNewThisWeek() {
        return newThisWeek;
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
