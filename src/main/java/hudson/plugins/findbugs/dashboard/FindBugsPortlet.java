package hudson.plugins.findbugs.dashboard;

import hudson.plugins.analysis.core.AbstractProjectAction;
import hudson.plugins.analysis.dashboard.AbstractWarningsGraphPortlet;
import hudson.plugins.findbugs.FindBugsProjectAction;

/**
 * A base class for portlets of the FindBugs plug-in.
 *
 * @author Ulli Hafner
 */
public abstract class FindBugsPortlet extends AbstractWarningsGraphPortlet {
    /**
     * Creates a new instance of {@link FindBugsPortlet}.
     *
     * @param name
     *            the name of the portlet
     */
    public FindBugsPortlet(final String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends AbstractProjectAction<?>> getAction() {
        return FindBugsProjectAction.class;
    }

    /** {@inheritDoc} */
    @Override
    protected String getPluginName() {
        return "findbugs";
    }
}
