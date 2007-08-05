package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.util.IOException2;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class FindBugsResult implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FindBugsResult.class.getName());
    /** Difference between this and the previous build. */
    private final int delta;
    /** The current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** The parsed FindBugs result. */
    @SuppressWarnings("Se")
    private transient WeakReference<JavaProject> project;
    /** The number of warnings in this build. */
    private final int numberOfWarnings;

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     *
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project) {
        owner = build;
        numberOfWarnings = project.getNumberOfWarnings();
        this.project = new WeakReference<JavaProject>(project);
        delta = 0;
    }

    /**
     * Creates a new instance of <code>FindBugsResult</code>.
     * @param build
     *            the current build as owner of this action
     * @param project
     *            the parsed FindBugs result
     * @param previousResult result of previous build
     */
    public FindBugsResult(final Build<?, ?> build, final JavaProject project, final FindBugsResult previousResult) {
        owner = build;
        numberOfWarnings = project.getNumberOfWarnings();
        this.project = new WeakReference<JavaProject>(project);
        delta = project.getNumberOfWarnings() - previousResult.getNumberOfWarnings();
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return "FindBugs Result";
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the numberOfWarnings.
     *
     * @return the numberOfWarnings
     */
    public int getNumberOfWarnings() {
        return numberOfWarnings;
    }

    /**
     * Returns the delta.
     *
     * @return the delta
     */
    public int getDelta() {
        return delta;
    }

    /**
     * Returns the associated project of this result.
     *
     * @return the associated project of this result.
     */
    public JavaProject getProject() {
        try {
            if (project == null) {
                loadResult();
            }
            JavaProject result = project.get();
            if (result == null) {
                loadResult();
            }
            return project.get();
        }
        catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files.", exception);
        }
        catch (InterruptedException exception) {
            LOGGER.log(Level.WARNING, "Failed to load FindBugs files: operation has been canceled.", exception);
        }
        return new JavaProject();
    }

    /**
     * Loads the FindBugs results and wraps them in a weak reference that might
     * get removed by the garbage collector.
     *
     * @throws IOException if the files could not be read
     * @throws InterruptedException if the operation has been canceled
     */
    private void loadResult() throws IOException, InterruptedException {
        try {
            JavaProject result = new FindBugsCounter(owner).findBugs();
            project = new WeakReference<JavaProject>(result);
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }

    /**
     * Returns the dynamic result of the FindBugs analysis (detail page for a package).
     *
     * @param packageName the packe name to get the result for
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @return the dynamic result of the FindBugs analysis (detail page for a package).
     */
    public FindBugsDetail getDynamic(final String packageName, final StaplerRequest request, final StaplerResponse response) {
        return new FindBugsDetail(owner, getProject(), packageName);
    }

    /**
     * Returns the number of warnings of the specified package in the previous build.
     *
     * @param packageName
     *            the package to return the warnings for
     * @return number of warnings of the specified package.
     */
    public int getPreviousNumberOfWarnings(final String packageName) {
        FindBugsResultAction action = owner.getAction(FindBugsResultAction.class);
        if (action.hasPreviousResult()) {
            return action.getPreviousResult().getResult().getProject().getNumberOfWarnings(packageName);
        }
        else {
            return 0;
        }
    }
}
