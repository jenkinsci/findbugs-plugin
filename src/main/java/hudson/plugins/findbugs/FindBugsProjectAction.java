package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Project;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Entry point to visualize the FindBugs trend graph. Drawing of the graph is
 * delegated to the associated {@link FindBugsResultAction}.
 *
 * @author Ulli Hafner
 */
public class FindBugsProjectAction implements Action {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -654316141132780561L;
    /** Project that owns this action. */
    private final Project<?, ?> project;

    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public FindBugsProjectAction(final Project<?, ?> project) {
        this.project = project;
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public Project<?, ?> getProject() {
        return project;
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Trend";
    }

    /** {@inheritDoc} */
    public String getIconFileName() {
        return null; // i.e., don't show the link in the side bar
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "findbugs";
    }

    /**
     * Display the warnings trend. Delegates to the the associated
     * {@link FindBugsResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link FindBugsResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doGraph(final StaplerRequest request, final StaplerResponse response)
            throws IOException {
        AbstractBuild<?, ?> lastBuild = project.getLastSuccessfulBuild();
        if (lastBuild != null) {
            FindBugsResultAction action = lastBuild.getAction(FindBugsResultAction.class);
            if (action != null) {
                action.doGraph(request, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}

