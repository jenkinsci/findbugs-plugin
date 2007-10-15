package hudson.plugins.findbugs;

import hudson.model.Build;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.util.ChartBuilder;
import hudson.util.ChartUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Base class for warning detail objects.
 */
public abstract class AbstractWarningsDetail implements ModelObject, Serializable, WarningProvider  {
    /** Current build as owner of this action. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** All fixed warnings in this build. */
    @SuppressWarnings("Se")
    private transient Set<Warning> warnings;
    /** All fixed warnings in this build. */
    @SuppressWarnings("Se")
    private transient Map<Integer, Warning> warningsByKey = new HashMap<Integer, Warning>();
    /** The number of low priority warnings in this build. */
    @SuppressWarnings("Se")
    private transient int low;
    /** The number of normal priority warnings in this build. */
    @SuppressWarnings("Se")
    private transient int normal;
    /** The number of high priority warnings in this build. */
    @SuppressWarnings("Se")
    private transient int high;

    /**
     * Creates a new instance of <code>AbstractWarningsDetail</code>.
     *
     * @param owner
     *            current build as owner of this action.
     * @param warnings
     *            the set of warnings represented by this object
     */
    public AbstractWarningsDetail(final Build<?, ?> owner, final Set<Warning> warnings) {
        this.owner = owner;
        this.warnings = warnings;

        computePriorities(warnings);
        computeWarningMapping(warnings);
    }

    /**
     * Returns the build as owner of this action.
     *
     * @return the owner
     */
    public final Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public final boolean isCurrent() {
        return owner.getProject().getLastBuild().number == owner.number;
    }

    /**
     * Returns the set of warnings.
     *
     * @return the set of warnings
     */
    public Set<Warning> getWarnings() {
        if (warnings == null) {
            warnings = new HashSet<Warning>();
        }
        return warnings;
    }

    /**
     * Creates a detail graph for the specified detail object.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     * @param detailObject
     *            the detail object to compute the graph for
     * @param upperBound
     *            the upper bound of all tasks
     * @throws IOException
     *             in case of an error
     */
    protected final void createDetailGraph(final StaplerRequest request, final StaplerResponse response,
            final WarningProvider detailObject, final int upperBound) throws IOException {
        if (ChartUtil.awtProblem) {
            response.sendRedirect2(request.getContextPath() + "/images/headless.png");
            return;
        }
        ChartBuilder chartBuilder = new ChartBuilder();
        JFreeChart chart = chartBuilder.createHighNormalLowChart(
                detailObject.getNumberOfHighWarnings(),
                detailObject.getNumberOfNormalWarnings(),
                detailObject.getNumberOfLowWarnings(), upperBound);
        ChartUtil.generateGraph(request, response, chart, 400, 20);
    }

    /**
     * Computes the low, normal and high priority count.
     *
     * @param allWarnings
     *            all project warnings
     */
    private void computePriorities(final Set<Warning> allWarnings) {
        low = WarningDifferencer.countLowPriorityWarnings(allWarnings);
        normal = WarningDifferencer.countNormalPriorityWarnings(allWarnings);
        high = WarningDifferencer.countHighPriorityWarnings(allWarnings);
    }

    /**
     * Computes a mapping of warnings by key.
     *
     * @param allWarnings
     *            all project warnings
     */
    protected final void computeWarningMapping(final Set<Warning> allWarnings) {
        if (warningsByKey == null) {
            warningsByKey = new HashMap<Integer, Warning>();
        }
        else {
            warningsByKey.clear();
        }
        int key = 0;
        for (Warning warning : allWarnings) {
            warning.setKey(key);
            warningsByKey.put(warning.getKey(), warning);
            key++;
        }
    }

    /**
     * Returns the warning with the specified key.
     *
     * @param key the warning key
     * @return the warning with the specified key.
     */
    public final Warning getWarning(final int key) {
        return warningsByKey.get(key);
    }

    /**
     * Returns the warning with the specified key.
     *
     * @param key the warning key
     * @return the warning with the specified key.
     */
    public final Warning getWarning(final String key) {
        return getWarning(Integer.valueOf(key));
    }

    /**
     * Returns the total number of warnings with priority LOW.
     *
     * @return the total number of warnings with priority LOW
     */
    public int getNumberOfLowWarnings() {
        return low;
    }

    /**
     * Returns the total number of warnings with priority HIGH.
     *
     * @return the total number of warnings with priority HIGH
     */
    public int getNumberOfHighWarnings() {
        return high;
    }

    /**
     * Returns the total number of warnings with priority NORMAL.
     *
     * @return the total number of warnings with priority NORMAL
     */
    public int getNumberOfNormalWarnings() {
        return normal;
    }

    /** {@inheritDoc} */
    public int getNumberOfWarnings() {
        return warnings.size();
    }
}
