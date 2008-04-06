package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.util.model.AnnotationContainer;
import hudson.plugins.findbugs.util.model.Priority;

/**
 * Creates priority detail objects.
 */
public class PriorityDetailFactory {
    /**
     * Returns whether the provided value is a valid priority.
     *
     * @param value the value to check
     * @return <code>true</code> if the provided value is a valid priority, <code>false</code> otherwise
     */
    public boolean isPriority(final String value) {
        for (Priority priority : Priority.values()) {
            if (priority.toString().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new priorities detail object.
     *
     * @param priority
     *            the priority to show
     * @param owner
     *            owner of the build
     * @param container
     *            annotation container
     * @param header
     *            header to show
     * @return the priority detail
     */
    public PrioritiesDetail create(final String priority, final AbstractBuild<?, ?> owner, final AnnotationContainer container, final String header) {
        if (Priority.HIGH.toString().equals(priority)) {
            return createPrioritiesDetail(Priority.HIGH, owner, container, header);
        }
        else if (Priority.NORMAL.toString().equals(priority)) {
            return createPrioritiesDetail(Priority.NORMAL, owner, container, header);
        }
        else if (Priority.LOW.toString().equals(priority)) {
            return createPrioritiesDetail(Priority.LOW, owner, container, header);
        }
        throw new IllegalArgumentException("Wrong priority provided: " + priority);
    }

    /**
     * Creates a new priorities detail.
     *
     * @param priority
     *            the priority to show
     * @param owner
     *            owner of the build
     * @param container
     *            annotation container
     * @param header
     *            header to show
     * @return the priority detail
     */
    private PrioritiesDetail createPrioritiesDetail(final Priority priority, final AbstractBuild<?, ?> owner, final AnnotationContainer container, final String header) {
        return new PrioritiesDetail(owner, container.getAnnotations(priority), priority, header);
    }
}

