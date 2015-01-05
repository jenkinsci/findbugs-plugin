package hudson.plugins.findbugs;

import com.thoughtworks.xstream.XStream;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildHistory;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.findbugs.parser.Bug;

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
    private int notInCloud;
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
     * @param usePreviousBuildAsReference
     *            determines whether to use the previous build as the reference
     *            build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     */
    public FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference) {
        this(build, defaultEncoding, result, usePreviousBuildAsReference, useStableBuildAsReference,
                FindBugsResultAction.class);
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
     * @param usePreviousBuildAsReference
     *            determines whether to use the previous build as the reference
     *            build
     * @param useStableBuildAsReference
     *            determines whether only stable builds should be used as
     *            reference builds or not
     * @param actionType
     *            the type of the result action
     */
    protected FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding, final ParserResult result,
            final boolean usePreviousBuildAsReference, final boolean useStableBuildAsReference,
            final Class<? extends ResultAction<FindBugsResult>> actionType) {
        this(build, new BuildHistory(build, actionType, usePreviousBuildAsReference, useStableBuildAsReference),
                result, defaultEncoding, true);
    }

    FindBugsResult(final AbstractBuild<?, ?> build, final BuildHistory history,
            final ParserResult result, final String defaultEncoding, final boolean canSerialize) {
        super(build, history, result, defaultEncoding);

        init();
        if (canSerialize) {
            serializeAnnotations(result.getAnnotations());
        }
    }

    private void init() {
        for (FileAnnotation annotation : getAnnotations()) {
            if (annotation instanceof Bug) {
                Bug bug = (Bug) annotation;
                if (bug.isInCloud()) {
                    if (bug.isShouldBeInCloud() && bug.getAgeInDays() <= LESS_ONE_WEEK) {
                        newThisWeek++;
                    }
                    numberOfComments += bug.getReviewCount();
                }
                else if (bug.isShouldBeInCloud()) {
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

    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("bug", Bug.class);
    }

    @Override
    public String getSummary() {
        return "FindBugs: " + createDefaultSummary(FindBugsDescriptor.RESULT_URL, getNumberOfAnnotations(), getNumberOfModules());
    }

    @Override
    protected String createDeltaMessage() {
        return createDefaultDeltaMessage(FindBugsDescriptor.RESULT_URL, getNumberOfNewWarnings(), getNumberOfFixedWarnings());
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

    @Override
    protected String getSerializationFileName() {
        return "findbugs-warnings.xml";
    }

    @Override
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return FindBugsResultAction.class;
    }
}
