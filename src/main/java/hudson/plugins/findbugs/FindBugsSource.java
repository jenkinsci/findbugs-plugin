package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
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
    /** Relative file name containing the corresponding task. */
    private final String fileName;
    /** The warning to be shown. */
    private final Warning warning;

    /**
     * Creates a new instance of <code>TaskDetail</code>.
     *
     * @param owner
     *            the current build as owner of this action
     * @param warning
     *            the warning to display in the source file
     */
    public FindBugsSource(final Build<?, ?> owner, final Warning warning) {
        this.owner = owner;
        this.warning = warning;
        fileName = StringUtils.substringAfterLast(warning.getFile(), "/");
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
    public String getContent() {
        InputStream file = null;
        try {
            String linkName = warning.getFile();
            if (linkName .startsWith("/") || linkName.contains(":")) {
                file = new FileInputStream(new File(linkName));
            }
            else {
                FilePath content = owner.getProject().getWorkspace().child(linkName);
                file = content.read();
            }

            return highlightSource(file);
        }
        catch (IOException exception) {
            return "Can't read file: " + exception.getLocalizedMessage();
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }


    /**
     * Highlights the specified source and returns the result as an HTML string.
     *
     * @param file the source file to highlight
     * @return the source as an HTML string
     * @throws IOException
     */
    public String highlightSource(final InputStream file) throws IOException {
        JavaSource source = new JavaSourceParser().parse(file);

        JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
        StringWriter writer = new StringWriter();
        JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
        options.setShowLineNumbers(true);
        options.setAddLineAnchors(true);
        converter.convert(source, options, writer);
        return writer.toString();
    }

//    public String getSourceFile() {
//        String file = getContent();
//
//        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(file));
//
//        int line = 1;
//        while (lineIterator.hasNext()) {
//            String content = lineIterator.nextLine();
//            line++;
//        }
//        return "";
//    }

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
}

