package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
import hudson.plugins.findbugs.util.AnnotationParser;
import hudson.plugins.findbugs.util.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

/**
 * A parser for FindBugs XML files.
 *
 * @author Ulli Hafner
 */
public class FindBugsParser implements AnnotationParser {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 8306319007761954027L;
    /** Workspace root. */
    private final FilePath workspace;
    /** Determines whether we need to initialize FindBugs or not. */
    private final boolean autoInitializeFindBugs;

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param workspace
     *            the workspace folder to be used as basis for source code
     *            mapping
     * @param autoInitializeFindBugs
     *            determines whether we need to initialize FindBugs or not
     */
    public FindBugsParser(final FilePath workspace, final boolean autoInitializeFindBugs) {
        this.workspace = workspace;
        this.autoInitializeFindBugs = autoInitializeFindBugs;
    }

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param workspace
     *            the workspace folder to be used as basis for source code
     *            mapping
     * @param autoInitializeFindBugs
     *            determines whether we need to initialize FindBugs or not
     */
    public FindBugsParser(final FilePath workspace, final boolean autoInitializeFindBugs, final List<String> sourceFolders) {
        this.workspace = workspace;
        this.autoInitializeFindBugs = autoInitializeFindBugs;
    }

    /** {@inheritDoc} */
    public String getName() {
        return "FINDBUGS";
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        Collection<FileAnnotation> annotations;
        try {
            MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();
            if (mavenFindBugsParser.accepts(new FileInputStream(file))) {
                return mavenFindBugsParser.parse(new FileInputStream(file), moduleName, workspace);
            }
            else {
                PlainFindBugsParser parser;
                if (autoInitializeFindBugs) {
                    parser = new NativeFindBugsParser();
                }
                else {
                    parser = new PlainFindBugsParser();
                }
                String moduleRoot = StringUtils.substringBefore(file.getAbsolutePath().replace('\\', '/'), "/target/");

                return parser.parse(new FileInputStream(file), moduleRoot, moduleName);
            }
        }
        catch (IOException exception) {
            throw new InvocationTargetException(exception);
        }
        catch (SAXException exception) {
            throw new InvocationTargetException(exception);
        }
        catch (DocumentException exception) {
            throw new InvocationTargetException(exception);
        }
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final InputStream file, final String moduleName) throws InvocationTargetException {
        throw new UnsupportedOperationException("FinBugs parser does not parse input streams.");
    }
}

