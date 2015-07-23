package hudson.plugins.findbugs;

import hudson.model.Run;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

import java.util.Collection;

/**
 * Detail view for the FindBugs plug-in: uses different table visualization.
 *
 * @author Ulli Hafner
 */
public class FindBugsTabDetail extends TabDetail {
    private static final long serialVersionUID = -3117538321276802327L;

    /**
     * Creates a new instance of {@link FindBugsTabDetail}.
     *
     * @param owner
     *            current build as owner of this action.
     * @param detailFactory
     *            the detail factory to use
     * @param annotations
     *            the module to show the details for
     * @param url
     *            URL to render the content of this tab
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     */
    public FindBugsTabDetail(final Run<?, ?> owner, final DetailFactory detailFactory, final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        super(owner, detailFactory, annotations, url, defaultEncoding);
    }

    @Override
    public String getWarnings() {
        return "findbugs-warnings.jelly";
    }
}

