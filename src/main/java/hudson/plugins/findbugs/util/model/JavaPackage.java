package hudson.plugins.findbugs.util.model;


/**
 * A serializable Java Bean class representing a Java package.
 *
 * @author Ulli Hafner
 */
public class JavaPackage extends AnnotationContainer {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 4034932648975191723L;
    // FIXME: Pull up
    /** Name of this package. */
    private final String name;

    /**
     * Creates a new instance of <code>JavaPackage</code>.
     *
     * @param packageName
     *            the name of this package
     */
    public JavaPackage(final String packageName) {
        super(true);

        name = packageName;
    }

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        rebuildMappings(true);
        return this;
    }

    /**
     * Returns the name of this package.
     *
     * @return the name of this package
     */
    public String getName() {
        return name;
    }
}

