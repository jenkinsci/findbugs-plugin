package io.jenkins.plugins.analysis.findbugs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.kohsuke.stapler.DataBoundConstructor;

import io.jenkins.plugins.analysis.core.steps.StaticAnalysisTool;

import hudson.Extension;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.findbugs.FindBugsDescriptor;
import hudson.plugins.findbugs.Messages;
import hudson.plugins.findbugs.parser.FindBugsParser;

/**
 * Provides customized messages for PMD.
 *
 * @author Ullrich Hafner
 */
public class FindBugs extends StaticAnalysisTool {
    private final boolean useRankAsPriority;

    /**
     * Creates a new instance of {@link FindBugs}.
     */
    @DataBoundConstructor
    public FindBugs(final boolean useRankAsPriority) {
        super(FindBugsDescriptor.PLUGIN_ID);
        this.useRankAsPriority = useRankAsPriority;
    }

    public boolean getUseRankAsPriority() {
        return useRankAsPriority;
    }

    @Override
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        return new FindBugsParser(useRankAsPriority).parse(file, moduleName);
    }

    @Override
    protected String getName() {
        return "FindBugs";
    }

    @Override
    public String getLinkName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    @Override
    public String getTrendName() {
        return Messages.FindBugs_Trend_Name();
    }

    @Override
    public String getSmallIconUrl() {
        return get().getIconUrl();
    }

    private FindBugsDescriptor get() {
        return new FindBugsDescriptor();
    }

    @Override
    public String getLargeIconUrl() {
        return get().getSummaryIconUrl();
    }

    /** Descriptor for FindBugs. */
    @Extension
    public static final class Descriptor extends StaticAnalysisToolDescriptor {
        public Descriptor() {
            super(FindBugs.class);
        }
    }
}
