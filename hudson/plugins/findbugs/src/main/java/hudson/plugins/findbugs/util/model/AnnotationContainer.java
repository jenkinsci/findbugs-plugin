package hudson.plugins.findbugs.util.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * A container for annotations.
 *
 * @author Ulli Hafner
 */
public class AnnotationContainer implements AnnotationProvider, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 855696821788264261L;

    /** The annotations mapped by their key. */
    private final Map<Long, FileAnnotation> annotations = new HashMap<Long, FileAnnotation>();
    /** The annotations mapped by priority. */
    private transient Map<Priority, Set<FileAnnotation>> annotationsByPriority;

    /**
     * Creates a new instance of <code>AnnotationContainer</code>.
     */
    public AnnotationContainer() {
        initializePrioritiesMap();
    }

    /**
     * Initializes the priorities maps.
     */
    private void initializePrioritiesMap() {
        annotationsByPriority = new EnumMap<Priority, Set<FileAnnotation>>(Priority.class);
        for (Priority priority : Priority.values()) {
            annotationsByPriority.put(priority, new HashSet<FileAnnotation>());
        }
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildPriorities();
        return this;
    }

    /**
     * Rebuilds the priorities after deserialization.
     */
    protected void rebuildPriorities() {
        initializePrioritiesMap();
        for (FileAnnotation annotation : getAnnotations()) {
            annotationsByPriority.get(annotation.getPriority()).add(annotation);
        }
    }

    /**
     * Adds the specified annotation to this container.
     *
     * @param annotation
     *            the annotation to add
     */
    public final void addAnnotation(final FileAnnotation annotation) {
        annotations.put(annotation.getKey(), annotation);
        annotationsByPriority.get(annotation.getPriority()).add(annotation);
        annotationAdded(annotation);
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addAnnotations(final Collection<? extends FileAnnotation> newAnnotations) {
        for (FileAnnotation annotation : newAnnotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Adds the specified annotations to this container.
     *
     * @param newAnnotations
     *            the annotations to add
     */
    public final void addAnnotations(final FileAnnotation[] newAnnotations) {
        addAnnotations(Arrays.asList(newAnnotations));
    }

    /**
     * Called if the specified annotation has been added to this container.
     * Subclasses may override this default empty implementation.
     *
     * @param annotation
     *            the added annotation
     */
    protected void annotationAdded(final FileAnnotation annotation) {
        // empty default implementation
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations() {
        return Collections.unmodifiableCollection(annotations.values());
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final Priority priority) {
        return Collections.unmodifiableCollection(annotationsByPriority.get(priority));
    }

    /** {@inheritDoc} */
    public final Collection<FileAnnotation> getAnnotations(final String priority) {
        return getAnnotations(getPriority(priority));
    }

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority
     *            priority as a String
     * @return enumeration value.
     */
    private Priority getPriority(final String priority) {
        return Priority.fromString(priority);
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations() {
        return annotations.size();
    }

    /** {@inheritDoc} */
    public int getNumberOfAnnotations(final Priority priority) {
        return annotationsByPriority.get(priority).size();
    }

    /** {@inheritDoc} */
    public final int getNumberOfAnnotations(final String priority) {
        return getNumberOfAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations() {
        return !annotations.isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final Priority priority) {
        return !annotationsByPriority.get(priority).isEmpty();
    }

    /** {@inheritDoc} */
    public final boolean hasAnnotations(final String priority) {
        return hasAnnotations(getPriority(priority));
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final long key) {
        FileAnnotation annotation = annotations.get(key);
        if (annotation != null) {
            return annotation;
        }
        throw new NoSuchElementException("Annotation not found: key=" + key);
    }

    /** {@inheritDoc} */
    public final FileAnnotation getAnnotation(final String key) {
        return getAnnotation(Long.parseLong(key));
    }

    /**
     * Returns a tooltip showing the distribution of priorities for this container.
     *
     * @return a tooltip showing the distribution of priorities
     */
    public String getToolTip() {
        StringBuilder message = new StringBuilder();
        for (Priority priority : Priority.values()) {
            if (hasAnnotations(priority)) {
                message.append(priority);
                message.append(":");
                message.append(getNumberOfAnnotations(priority));
                message.append(" - ");
            }
        }
        return StringUtils.removeEnd(message.toString(), " - ");
    }
}

