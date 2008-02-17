package hudson.plugins.findbugs.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.plugins.findbugs.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;

/**
 * Renders a source file containing an annotation for the whole file or a
 * specific line number.
 */
public class SourceDetail implements ModelObject {
    /** Offset of the source code generator. After this line the actual source file lines start. */
    protected static final int SOURCE_GENERATOR_OFFSET = 12;
    /** The current build as owner of this object. */
    private final AbstractBuild<?, ?> owner;
    /** Stripped file name of this annotation without the path prefix. */
    private final String fileName;
    /** The annotation to be shown. */
    private final FileAnnotation annotation;
    /** The line containing identified by the annotation or an empty string. */
    private String line = StringUtils.EMPTY;
    /** The source code after the annotation or an empty string. */
    private String suffix = StringUtils.EMPTY;
    /** The source code before the warning or the whole source code if there is no specific line given. */
    private String prefix = StringUtils.EMPTY;

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
        fileName = StringUtils.substringAfterLast(annotation.getWorkspaceFile().getName(), "/");

        initializeContent();
    }

    /**
     * Initializes the content of the source file: reads the file, colors it, and
     * splits it into three parts.
     */
    private void initializeContent() {
        InputStream file = null;
        try {
            String linkName = annotation.getWorkspaceFile().getName();
            if (linkName.startsWith("/") || linkName.contains(":") || owner == null) {
                file = new FileInputStream(new File(linkName));
            }
            else {
                file = owner.getProject().getWorkspace().child(linkName).read();
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

    /** {@inheritDoc} */
    public String getDisplayName() {
        return fileName;
    }

    /**
     * Returns the tool tip to be shown if hovering over the highlighted line.
     *
     * @return the tool tip to be shown
     */
    public String getToolTip() {
        return annotation.getToolTip();
    }

    /**
     * Returns the tool tip to be shown if hovering over the highlighted line.
     *
     * @return the tool tip to be shown
     */
    public String getMessage() {
        return annotation.getMessage();
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
    public final void splitSourceFile(final String sourceFile) {
        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));

        if (annotation.isLineAnnotation()) {
            StringBuilder prefixBuilder = new StringBuilder(sourceFile.length());
            StringBuilder suffixBuilder = new StringBuilder(sourceFile.length());

            suffixBuilder.append("</td></tr>\n");
            suffixBuilder.append("<tr><td>\n");
            suffixBuilder.append("<code>\n");

            int warningLine = annotation.getLineNumber();
            int lineNumber = 1;
            while (lineIterator.hasNext()) {
                String content = lineIterator.nextLine();
                if (lineNumber - SOURCE_GENERATOR_OFFSET == warningLine) {
                    line = content;
                }
                else if (lineNumber - SOURCE_GENERATOR_OFFSET < warningLine) {
                    prefixBuilder.append(content + "\n");
                }
                else {
                    suffixBuilder.append(content + "\n");
                }
                lineNumber++;
            }

            prefixBuilder.append("</code>\n");
            prefixBuilder.append("</td></tr>\n");
            prefixBuilder.append("<tr><td bgcolor=\"#FFFFC0\">\n");

            prefix = prefixBuilder.toString();
            suffix = suffixBuilder.toString();
        }
        else {
            prefix = sourceFile;
            suffix = StringUtils.EMPTY;
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
    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the line that should be highlighted.
     *
     * @return the line to highlight
     */
    public String getLine() {
        return line;
    }

    /**
     * Returns whether this source code object has an highlighted line.
     *
     * @return <code>true</code> if this source code object has an highlighted
     *         line
     */
    public boolean hasHighlightedLine() {
        return StringUtils.isNotEmpty(line);
    }

    /**
     * Returns the suffix of the source file. The suffix contains the part of
     * the source after the line to highlight.
     *
     * @return the suffix of the source file
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Returns the prefix of the source file. The prefix contains the part of
     * the source before the line to highlight.
     *
     * @return the prefix of the source file
     */
    public String getPrefix() {
        return prefix;
    }
}

