package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;

import java.util.NoSuchElementException;

import org.kohsuke.stapler.StaplerProxy;

/**
 * Controls the live cycle of the FindBugs results. This action persists the
 * results of the FindBugs analysis of a build and displays the results on the
 * build page. The actual visualization of the results is defined in the
 * matching <code>summary.jelly</code> file.
 * <p>
 * TODO: Add a page that visualizes the FindBugs results
 * </p>
 */
public class FindBugsBuildAction implements StaplerProxy, HealthReportingAction {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -5329651349674842873L;
    /** The associated build of this action. */
    private final transient Build<?, ?> owner;

    /** The actual result of the FindBugs analysis. */
    private FindBugsResult result;

    /**
     * Creates a new instance of <code>FindBugsBuildAction</code>.
     *
     * @param owner
     *            the associated build of this action
     */
    public FindBugsBuildAction(final Build<?, ?> owner) {
        this.owner = owner;
    }

    /**
     * Returns the associated build of this action.
     *
     * @return the associated build of this action
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /** {@inheritDoc} */
    public Object getTarget() {
        return getResult();
    }

    /**
     * Returns the FindBugs result.
     *
     * @return the FindBugs result
     */
    public FindBugsResult getResult() {
        return result;
    }

    /** {@inheritDoc} */
    public HealthReport getBuildHealth() {
        // FIXME: Here should be done some evaluation
        return new HealthReport(100, "Everything is fine :-)");
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Results";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return "graph.gif";
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return null;
    }

    /**
     * Gets the FindBugs result of the previous build.
     *
     * @return the FindBugs result of the previous build.
     * @throws NoSuchElementException if there is no previous build for this action
     */
    public FindBugsBuildAction getPreviousResult() {
        FindBugsBuildAction previousBuild = getPreviousBuild();
        if (previousBuild == null) {
            throw new NoSuchElementException("There is no previous build for action " + this);
        }
        return previousBuild;
    }

    /**
     * Gets the test result of a previous build, if it's recorded, or <code>null</code> if not.
     *
     * @return the test result of a previous build, or <code>null</code>
     */
    private FindBugsBuildAction getPreviousBuild() {
        AbstractBuild<?, ?> build = owner;
        while (true) {
            build = build.getPreviousBuild();
            if (build == null) {
                return null;
            }
            FindBugsBuildAction action = build.getAction(FindBugsBuildAction.class);
            if (action != null) {
                return action;
            }
        }
    }

    /**
     * Returns whether a previous build already did run with FindBugs.
     *
     * @return <code>true</code> if a previous build already did run with
     *         FindBugs.
     */
    public boolean hasPreviousResult() {
        return getPreviousBuild() != null;
    }

    /**
     * Sets the FindBugs result for this build. The specified result will be persisted in the build folder
     * as an XML file.
     *
     * @param result the result to set
     */
    public void setResult(final FindBugsResult result) {
        this.result = result;
    }
}
