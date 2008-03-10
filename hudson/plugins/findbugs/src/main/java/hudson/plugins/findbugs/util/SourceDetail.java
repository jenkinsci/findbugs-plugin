package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.LineRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * Renders a source file containing an annotation for the whole file or a
 * specific line number.
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class SourceDetail implements ModelObject {
    /** Offset of the source code generator. After this line the actual source file lines start. */
    protected static final int SOURCE_GENERATOR_OFFSET = 13;
    /** Color for the first (primary) annotation range. */
    private static final String FIRST_COLOR = "#FCAF3E";
    /** Color for all other annotation ranges. */
    private static final String OTHER_COLOR = "#FCE94F";
    /** The current build as owner of this object. */
    private final AbstractBuild<?, ?> owner;
    /** Stripped file name of this annotation without the path prefix. */
    private final String fileName;
    /** The annotation to be shown. */
    private final FileAnnotation annotation;
    /** The rendered source file. */
    private String sourceCode = StringUtils.EMPTY;

    /**
     * Creates a new instance of this source code object.
     *
     * @param owner
     *            the current build as owner of this object
     * @param annotation
     *            the warning to display in the source file
     */
    public SourceDetail(final AbstractBuild<?, ?> owner, final FileAnnotation annotation) {
        this.owner = owner;
        this.annotation = annotation;
        fileName = StringUtils.substringAfterLast(annotation.getFileName(), "/");

        initializeContent();
    }

    /**
     * Initializes the content of the source file: reads the file, colors it, and
     * splits it into three parts.
     */
    private void initializeContent() {
        InputStream file = null;
        try {
            String linkName = annotation.getFileName();
            if (linkName.startsWith("/") || linkName.contains(":") || owner == null) {
                file = new FileInputStream(new File(linkName));
            }
            else {
                file = owner.getProject().getWorkspace().child(linkName).read();
            }
            splitSourceFile(highlightSource(file));
        }
        catch (IOException exception) {
            sourceCode = "Can't read file: " + exception.getLocalizedMessage();
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return fileName;
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
     * Splits the source code into three blocks: the line to highlight and the
     * source code before and after this line.
     *
     * @param sourceFile
     *            the source code of the whole file as rendered HTML string
     */
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public final void splitSourceFile(final String sourceFile) {
        StringBuilder output = new StringBuilder(sourceFile.length());

        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));
        int lineNumber = 1;

        try {
            while (lineNumber < SOURCE_GENERATOR_OFFSET) {
                output.append(lineIterator.nextLine());
                output.append("\n");
                lineNumber++;
            }
            lineNumber = 1;
            int ranges = 1;
            for (LineRange range : annotation.getLineRanges()) {
                while (lineNumber < range.getStart()) {
                    output.append(lineIterator.nextLine());
                    output.append("\n");
                    lineNumber++;
                }
                output.append("</code>\n");
                output.append("</td></tr>\n");
                output.append("<tr><td bgcolor=\"");
                if (ranges == 1) {
                    output.append(FIRST_COLOR);
                }
                else {
                    output.append(OTHER_COLOR);
                }
                output.append("\">\n");
                output.append("<div tooltip=\"");
                if (range.getStart() > 0) {
                    output.append(StringEscapeUtils.escapeHtml(annotation.getMessage()));
                }
                output.append(StringEscapeUtils.escapeHtml(annotation.getToolTip()));
                output.append("\" nodismiss=\"\">\n");
                output.append("<code><b>\n");
                if (range.getStart() <= 0) {
                    output.append(annotation.getMessage());
                    if (StringUtils.isBlank(annotation.getMessage())) {
                        output.append(annotation.getToolTip());
                    }
                }
                else {
                    while (lineNumber <= range.getEnd()) {
                        output.append(lineIterator.nextLine());
                        output.append("\n");
                        lineNumber++;
                    }
                }
                output.append("</b></code>\n");
                output.append("</div>\n");
                output.append("</td></tr>\n");
                output.append("<tr><td>\n");
                output.append("<code>\n");
                ranges++;
            }
            while (lineIterator.hasNext()) {
                output.append(lineIterator.nextLine());
                output.append("\n");
            }
        }
        catch (NoSuchElementException exception) {
            // ignore an illegal range
        }
        sourceCode = output.toString();
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
    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the line that should be highlighted.
     *
     * @return the line to highlight
     */
    public String getSourceCode() {
        return sourceCode;
    }
}

