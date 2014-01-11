package hudson.plugins.findbugs;

import hudson.plugins.analysis.views.DetailFactory;

/**
 * Initialization of Maven classes.
 *
 * @author Ulli Hafner
 */
public final class MavenInitialization {
    /**
     * Initializes the detail builder for Maven builds.
     *
     * @param detailBuilder
     *            the builder to use
     */
    public static void run(final DetailFactory detailBuilder) {
        DetailFactory.addDetailBuilder(FindBugsMavenResultAction.class, detailBuilder);
    }

    /**
     * Creates a new instance of {@link MavenInitialization}.
     */
    private MavenInitialization() {
        // prevents instantiation
    }
}
