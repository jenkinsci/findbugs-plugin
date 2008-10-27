package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
import hudson.plugins.findbugs.util.AnnotationParser;
import hudson.plugins.findbugs.util.model.FileAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

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
    /** Collection of source folders. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("Se")
    private final Collection<String> mavenSources;

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param workspace
     *            the workspace folder to be used as basis for source code
     *            mapping
     */
    public FindBugsParser(final FilePath workspace) {
        this(workspace, new ArrayList<String>());
    }

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param workspace
     *            the workspace folder to be used as basis for source code
     *            mapping
     * @param mavenSources
     *            a collection of folders to scan for source files. If empty,
     *            the source folders are guessed.
     */
    public FindBugsParser(final FilePath workspace, final Collection<String> mavenSources) {
        this.workspace = workspace;
        this.mavenSources = new ArrayList<String>(mavenSources);
    }

    /** {@inheritDoc} */
    public String getName() {
        return "FINDBUGS";
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();
            if (mavenFindBugsParser.accepts(new FileInputStream(file))) {
                return mavenFindBugsParser.parse(new FileInputStream(file), moduleName, workspace);
            }
            else {
                Collection<String> sources = new ArrayList<String>(mavenSources);
                if (sources.isEmpty()) {
                    String moduleRoot = StringUtils.substringBefore(file.getAbsolutePath().replace('\\', '/'), "/target/");
                    sources.add(moduleRoot + "/src/main/java");
                    sources.add(moduleRoot + "/src/test/java");
                    sources.add(moduleRoot + "/src");
                }
                return new NativeFindBugsParser().parse(file, sources, moduleName);
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
}

