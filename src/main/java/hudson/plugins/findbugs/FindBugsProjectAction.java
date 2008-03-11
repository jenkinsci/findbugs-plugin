package hudson.plugins.findbugs;

import hudson.model.AbstractProject;
import hudson.plugins.findbugs.util.AbstractProjectAction;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Entry point to visualize the FindBugs trend graph in the project screen.
 * Drawing of the graph is delegated to the associated
 * {@link FindBugsResultAction}.
 *
 * @author Ulli Hafner
 */
public class FindBugsProjectAction extends AbstractProjectAction<FindBugsResultAction> {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -654316141132780561L;
    /** Logger. */
    private final Logger LOGGER = Logger.getLogger(FindBugsProjectAction.class.getName());
    /**
     * Instantiates a new find bugs project action.
     *
     * @param project
     *            the project that owns this action
     */
    public FindBugsProjectAction(final AbstractProject<?, ?> project) {
        super(project, FindBugsResultAction.class, FindBugsDescriptor.FINDBUGS_ACTION_LOGO, "../lastBuild/findbugsResult");
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        LocaleProvider.setProvider(new LocaleProvider() {
            @Override
            public Locale get() {
                Locale locale = null;
                StaplerRequest req = Stapler.getCurrentRequest();
                if (req != null) {
                    locale = req.getLocale();
                    LOGGER.log(Level.INFO, req.getLocale().toString());
                }
                if (locale==null) {
                    locale = Locale.getDefault();
                }
                return locale;
            }
        });
        StaplerRequest req = Stapler.getCurrentRequest();
        if (req != null) {
            LOGGER.log(Level.INFO, "Locale of request");
            LOGGER.log(Level.INFO, req.getLocale().toString());
        }
        else {
            LOGGER.log(Level.INFO, "No request.");
        }

        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    public String getUrlName() {
        return "findbugs";
    }

    /** {@inheritDoc} */
    @Override
    protected String getCookieName() {
        return "FindBugs_displayMode";
    }
}

