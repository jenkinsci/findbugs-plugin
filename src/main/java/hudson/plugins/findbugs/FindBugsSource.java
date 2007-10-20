package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;
import hudson.model.ModelObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Renders a source file containing a warning or task at a given line.
 */
public class FindBugsSource implements ModelObject, Serializable {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3209724023376797741L;
    /** Offset of the source code generator. After this line the actual source file lines start. */
    protected static final int SOURCE_GENERATOR_OFFSET = 12;
    /** The current build as owner of this object. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** Relative file name containing the corresponding task. */
    private final String fileName;
    /** The warning to be shown. */
    private final Warning warning;
    /** The line containing the warning. */
    private String line;
    /** The source code after the warning. */
    private String suffixSource = StringUtils.EMPTY;
    /** The source code before the warning. */
    private String prefixSource = StringUtils.EMPTY;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *            the current build as owner of this object
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
     * Initializes the content of the source file: reads the file, colors it, and
     * splits it into three parts.
     */
    public final void initializeContent() {
        InputStream file = null;
        try {
            String linkName = warning.getFile();
            if (linkName.startsWith("/") || linkName.contains(":")) {
                file = new FileInputStream(new File(linkName));
            }
            else {
                FilePath content = owner.getProject().getWorkspace().child(linkName);
                file = content.read();
            }

            splitSourceFile(highlightSource(file));
        }
        catch (IOException exception) {
            line = "Can't read file: " + exception.getLocalizedMessage();
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    /**
     * Highlights the specified source and returns the result as an HTML string.
     *
     * @param file
     *            the source file to highlight
     * @return the source as an HTML string
     * @throws IOException
     */
    public final String highlightSource(final InputStream file) throws IOException {
        JavaSource source = new JavaSourceParser().parse(file);

        JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
        StringWriter writer = new StringWriter();
        JavaSourceConversionOptions options = JavaSourceConversionOptions.getDefault();
        options.setShowLineNumbers(true);
        options.setAddLineAnchors(true);
        converter.convert(source, options, writer);
        return writer.toString();
    }

    /**
     * Returns the tool tip to be shown if hovering over the highlighted line.
     *
     * @return the tool tip to be shown
     */
    public String getToolTip() {
        return warning.getDescription();
    }

    /**
     * Returns the tool tip to be shown if hovering over the highlighted line.
     *
     * @return the tool tip to be shown
     */
    public String getMessage() {
        return warning.getMessage();
    }

    /**
     * Splits the source code into three blocks: the line to highlight and the
     * source code before and after this line.
     *
     * @param sourceFile
     *            the source code of the whole file as rendered HTML string
     */
    public final void splitSourceFile(final String sourceFile) {
        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));

        String lineToHighlight = warning.getLineNumber();
        int warningLine;
        try {
            warningLine = Integer.parseInt(lineToHighlight);
            StringBuilder prefix = new StringBuilder(sourceFile.length());
            StringBuilder suffix = new StringBuilder(sourceFile.length());

            suffix.append("</td></tr>\n");
            suffix.append("<tr><td>\n");
            suffix.append("<code>\n");

            int lineNumber = 1;
            while (lineIterator.hasNext()) {
                String content = lineIterator.nextLine();
                if (lineNumber - SOURCE_GENERATOR_OFFSET == warningLine) {
                    line = content;
                }
                else if (lineNumber - SOURCE_GENERATOR_OFFSET < warningLine) {
                    prefix.append(content + "\n");
                }
                else {
                    suffix.append(content + "\n");
                }
                lineNumber++;
            }

            prefix.append("</code>\n");
            prefix.append("</td></tr>\n");
            prefix.append("<tr><td bgcolor=\"#FFFFC0\">\n");

            prefixSource = prefix.toString();
            suffixSource = suffix.toString();
        }
        catch (NumberFormatException e) {
            prefixSource = sourceFile;
            suffixSource = StringUtils.EMPTY;
            line = StringUtils.EMPTY;
        }
    }

    /**
     * Gets the file name of this source file.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the build
     */
    public Build<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the line that should be highlighted.
     *
     * @return the line to highlight
     */
    public String getLine() {
        if (line == null) {
            initializeContent();
        }
        return line;
    }

    /**
     * Returns whether this source code object has an highlighted line.
     *
     * @return <code>true</code> if this source code object has an highlighted
     *         line
     */
    public boolean hasHighlightedLine() {
        if (line == null) {
            initializeContent();
        }
        return StringUtils.isNotEmpty(line);
    }

    /**
     * Returns the suffix of the source file. The suffix contains the part of
     * the source after the line to highlight.
     *
     * @return the suffix of the source file
     */
    public String getSuffix() {
        if (line == null) {
            initializeContent();
        }
        return suffixSource;
    }

    /**
     * Returns the prefix of the source file. The prefix contains the part of
     * the source before the line to highlight.
     *
     * @return the prefix of the source file
     */
    public String getPrefix() {
        if (line == null) {
            initializeContent();
        }
        return prefixSource;
    }
}

