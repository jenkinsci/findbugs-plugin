package hudson.plugins.findbugs;

import java.util.Collection;

import hudson.model.Run;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.views.DetailFactory;
import hudson.plugins.analysis.views.TabDetail;

/**
 * A detail factory that creates a FindBugs specific warnings table.
 *
 * @author Ulli Hafner
 */
public class FindBugsDetailFactory extends DetailFactory {
    @Override
    protected TabDetail createTabDetail(final Run<?, ?> owner,
            final Collection<FileAnnotation> annotations, final String url, final String defaultEncoding) {
        return new FindBugsTabDetail(owner, this, annotations, url, defaultEncoding);
    }
}

