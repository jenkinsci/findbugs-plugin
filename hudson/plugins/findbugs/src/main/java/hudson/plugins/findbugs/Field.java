package hudson.plugins.findbugs;

/**
 * A FindBugs field.  This represents a field that may be associated with a
 * warning.
 */
public class Field {
    /** The qualified name of the class this field belongs to. */
    private String classname;
    /** The name of the field. */
    private String name;
    /** The field signature */
    private String signature;
    /** A field to indicate if this field is static. */
    private boolean isStatic;

    /**
     * Gets the class name.
     * 
     * @return The class name.
     */
    public String getClassname() {
        return classname;
    }

    /**
     * Sets the class name of this field.
     * 
     * @param classname The class name.
     */
    public void setClassname(String classname) {
        this.classname = classname;
    }

    /**
     * Gets the static indicator for this field.
     * 
     * @return isStatic Whether or not this field is static.
     */
    public boolean isIsStatic() {
        return isStatic;
    }

    /**
     * Sets the static indicator for this field.
     * 
     * @param isStatic The static indicator.
     */
    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    /**
     * Gets the name of this field.
     * 
     * @return name The name of this field.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this field.
     * 
     * @param name The name of this field.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the signature of this field.
     * 
     * @return signature The signature of this field.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the signature of this field.
     * 
     * @param signature The signature of this field.
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

}
