package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Renders a file containing an open task.
 */
public class FindBugsSource implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3209724023376797741L;
    /** The current build as owner of this object. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** Link to the file containing the corresponding task. */
    private final String linkName;
    /** Relative file name containing the corresponding task. */
    private final String fileName;

    /**
     * Creates a new instance of <code>TaskDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param link
     *            the link to the source in the workspace
     */
    public FindBugsSource(final Build<?, ?> owner, final String link) {
        this.owner = owner;
        linkName = link.replace('!', '/');
        fileName = StringUtils.substringAfterLast(link, "!");
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return fileName;
    }

    /**
     * Returns the content of the file in colored HTML.
     *
     * @return content of the file in HTML
     */
    @java.lang.SuppressWarnings("unchecked")
    public String getContent() {
        try {
            FilePath content = owner.getProject().getWorkspace().child(linkName);
            InputStream file = content.read();
            JavaSource source = new JavaSourceParser().parse(file);
            file.close();

            JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
            StringWriter writer = new StringWriter();
            JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
            options.setShowLineNumbers(true);
            options.setAddLineAnchors(true);
            converter.convert(source, options, writer);
            return writer.toString();
        }
        catch (IOException exception) {
            return "Can't read file: " + exception.getLocalizedMessage();
        }
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the build.
     *
     * @return the build
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the linkName.
     *
     * @return the linkName
     */
    public String getLinkName() {
        return linkName;
    }
}

