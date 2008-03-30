package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A project action displays a link on the side panel of a project.
 *
 * @param <T>
 *            result action type
 * @author Ulli Hafner
 */
public abstract class AbstractProjectAction<T extends ResultAction<?>> implements Action  {
    /** One year (in seconds). */
    private static final int ONE_YEAR = 60 * 60 * 24 * 365;
    /** Project that owns this action. */
    @SuppressWarnings("Se")
    private final AbstractProject<?, ?> project;
    /** The type of the result action.  */
    private final Class<T> resultActionType;
    /** The icon URL of this action: it will be shown as soon as a result is available. */
    private final String iconUrl;
    /** URL to the results of the last build. */
    private final String resultsUrl;

    /**
     * Creates a new instance of <code>AbstractProjectAction</code>.
     *
     * @param project
     *            the project that owns this action
     * @param resultActionType
     *            the type of the result action
     * @param iconUrl
     *            the icon URL of this action: it will be shown as soon as a
     *            result is available.
     * @param pluginName
     *            the plug-in name
     */
    public AbstractProjectAction(final AbstractProject<?, ?> project, final Class<T> resultActionType, final String iconUrl, final String pluginName) {
        this.project = project;
        this.resultActionType = resultActionType;
        this.iconUrl = iconUrl;
        this.resultsUrl = "../lastBuild/" + pluginName + "Result";
    }

    /**
     * Returns whether we should display the toggle graph type links.
     *
     * @return <code>true</code> if we should display the toggle graph type
     *         links
     */
    public final boolean isHealthinessEnabled() {
        ResultAction<?> lastAction = getLastAction();
        if (lastAction != null) {
            return lastAction.getHealthReportBuilder().isEnabled();
        }
        return false;
    }

    /**
     * Returns the project.
     *
     * @return the project
     */
    public final AbstractProject<?, ?> getProject() {
        return project;
    }

    /**
     * Returns whether we have enough valid results in order to draw a
     * meaningful graph.
     *
     * @param build
     *            the build to look backward from
     * @return <code>true</code> if the results are valid in order to draw a
     *         graph
     */
    public final boolean hasValidResults(final AbstractBuild<?, ?> build) {
        if (build != null) {
            ResultAction<?> resultAction = build.getAction(resultActionType);
            if (resultAction != null) {
                return resultAction.hasPreviousResultAction();
            }
        }
        return false;
    }

    /**
     * Returns the icon URL for the side-panel in the project screen. If there
     * is yet no valid result, then <code>null</code> is returned.
     *
     * @return the icon URL for the side-panel in the project screen
     */
    public String getIconFileName() {
        if (getLastAction() != null) {
            return iconUrl;
        }
        return null;
    }

    /**
     * Returns the last valid result action.
     *
     * @return the last valid result action, or <code>null</code> if no such action is found
     */
    public ResultAction<?> getLastAction() {
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        if (lastBuild != null) {
            return lastBuild.getAction(resultActionType);
        }
        return null;
    }

    /**
     * Display the trend graph. Delegates to the the associated
     * {@link ResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link ResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    public void doTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        createGraph(request, response);
    }

    /**
     * Creates a trend graph or map.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error in
     *             {@link ResultAction#doGraph(StaplerRequest, StaplerResponse)}
     */
    private void createGraph(final StaplerRequest request, final StaplerResponse response)
            throws IOException {
        ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            action.doGraph(request, response);
        }
    }

    /**
     * Display the trend map. Delegates to the the associated
     * {@link ResultAction}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doTrendMap(final StaplerRequest request, final StaplerResponse response) throws IOException {
        ResultAction<?> action = getLastAction();
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else {
            action.doGraphMap(request, response);
        }
    }

    /**
     *
     * Redirects the index page to the last result.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        response.sendRedirect2(resultsUrl);
    }

    /**
     * Changes the trend graph display mode.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @throws IOException
     *             in case of an error
     */
    public void doFlipTrend(final StaplerRequest request, final StaplerResponse response) throws IOException {
        boolean useHealthBuilder = true;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(getCookieName())) {
                    useHealthBuilder = Boolean.parseBoolean(cookie.getValue());
                }
            }
        }

        useHealthBuilder = !useHealthBuilder;

        Cookie cookie = new Cookie(getCookieName(), String.valueOf(useHealthBuilder));
        List<?> ancestors = request.getAncestors();
        Ancestor ancestor = (Ancestor) ancestors.get(ancestors.size() - 2);
        cookie.setPath(ancestor.getUrl());
        cookie.setMaxAge(ONE_YEAR);
        response.addCookie(cookie);

        response.sendRedirect("..");
    }

    /**
     * Returns the flip trend cookie name.
     *
     * @return the flip trend cookie name.
     */
    protected abstract String getCookieName();
}
