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
 * Renders a file containing a FindBugs warning.
 */
public class FindBugsSource implements ModelObject, Serializable {
    /** Offset of the source code generator. After this line the actual source file lines start. */
    protected static final int SOURCE_GENERATOR_OFFSET = 12;
    /** Unique identifier of this class. */
    private static final long serialVersionUID = -3209724023376797741L;
    /** The current build as owner of this object. */
    @SuppressWarnings("Se")
    private final Build<?, ?> owner;
    /** Relative file name containing the corresponding task. */
    private final String fileName;
    /** The warning to be shown. */
    private final Warning warning;
    /** The line containing the warning. */
    private String actualWarningLine;
    /** The source code after the warning. */
    private String suffixSource = StringUtils.EMPTY;
    /** The source code before the warning. */
    private String prefixSource = StringUtils.EMPTY;

    /**
     * Creates a new instance of <code>FindBugsSource</code>.
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
     * Initializes the content of the source file: reads the file, colors it,
     * splits it into three parts.
     */
    public final void initializeContent() {
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

            splitSourceFile(highlightSource(file));
        }
        catch (IOException exception) {
            actualWarningLine = "Can't read file: " + exception.getLocalizedMessage();
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
     * Splits the source code into three blocks: source code before and after the
     * warning and the warning line itself.
     *
     * @param sourceFile
     *            the source code as HTML string
     */
    public final void splitSourceFile(final String sourceFile) {
        LineIterator lineIterator = IOUtils.lineIterator(new StringReader(sourceFile));

        String lineNumber = warning.getLineNumber();
        int warningLine;
        try {
            warningLine = Integer.parseInt(lineNumber);
            StringBuilder prefix = new StringBuilder(sourceFile.length());
            StringBuilder suffix = new StringBuilder(sourceFile.length());

            suffix.append("</td></tr>\n");
            suffix.append("<tr><td>\n");
            int line = 1;
            while (lineIterator.hasNext()) {
                String content = lineIterator.nextLine();
                if (line - SOURCE_GENERATOR_OFFSET == warningLine) {
                    actualWarningLine = content;
                }
                else if (line - SOURCE_GENERATOR_OFFSET < warningLine) {
                    prefix.append(content + "\n");
                }
                else {
                    suffix.append(content + "\n");
                }
                line++;
            }
            prefix.append("</td></tr>\n");
            prefix.append("<tr><td bgcolor=\"#FFFFC0\">\n");

            prefixSource = prefix.toString();
            suffixSource = suffix.toString();
        }
        catch (NumberFormatException e) {
            prefixSource = sourceFile;
            suffixSource = StringUtils.EMPTY;
            actualWarningLine = StringUtils.EMPTY;
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
     * Returns the actualWarningLine.
     *
     * @return the actualWarningLine
     */
    public String getWarningLine() {
        if (actualWarningLine == null) {
            initializeContent();
        }
        return actualWarningLine;
    }

    /**
     * Returns the suffixSource.
     *
     * @return the suffixSource
     */
    public String getSuffix() {
        if (actualWarningLine == null) {
            initializeContent();
        }
        return suffixSource;
    }

    /**
     * Returns the prefixSource.
     *
     * @return the prefixSource
     */
    public String getPrefix() {
        if (actualWarningLine == null) {
            initializeContent();
        }
        return prefixSource;
    }
}

