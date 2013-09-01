package hudson.plugins.findbugs;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.plugins.analysis.views.WarningsCountColumn;

import hudson.views.ListViewColumnDescriptor;

/**
 * A column that shows the total number of FindBugs warnings in a job.
 *
 * @author Ulli Hafner
 */
public class FindBugsColumn extends WarningsCountColumn<FindBugsProjectAction> {
    /**
     * Creates a new instance of {@link FindBugsColumn}.
     */
    @DataBoundConstructor
    public FindBugsColumn() { // NOPMD: data binding
        super();
    }

    @Override
    protected Class<FindBugsProjectAction> getProjectAction() {
        return FindBugsProjectAction.class;
    }

    @Override
    public String getColumnCaption() {
        return Messages.FindBugs_Warnings_ColumnHeader();
    }

    /**
     * Descriptor for the column.
     */
    @Extension
    public static class ColumnDescriptor extends ListViewColumnDescriptor {
        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.FindBugs_Warnings_Column();
        }
    }
}
