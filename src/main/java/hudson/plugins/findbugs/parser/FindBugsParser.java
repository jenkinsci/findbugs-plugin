package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.plugins.findbugs.Messages;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
import hudson.plugins.findbugs.util.AnnotationParser;
import hudson.plugins.findbugs.util.model.MavenModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
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
    public MavenModule parse(final File file, final String moduleName) throws InvocationTargetException {
        Exception exception = null;
        MavenModule module = new MavenModule(moduleName);
        try {
            MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();
            if (mavenFindBugsParser.accepts(new FileInputStream(file))) {
                module = mavenFindBugsParser.parse(new FileInputStream(file), moduleName, workspace);
                module.setError(Messages.FindBugs_FindBugsCollector_Error_OldMavenPlugin(file.getAbsolutePath()));
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
                module = parser.parse(new FileInputStream(file), moduleRoot, moduleName);
            }
        }
        catch (IOException e) {
            exception = e;
        }
        catch (SAXException e) {
            exception = e;
        }
        catch (DocumentException e) {
            exception = e;
        }
        catch (InterruptedException e) {
            // ignore this exception and return
        }
        if (exception != null) {
            String errorMessage = Messages.FindBugs_FindBugsCollector_Error_Exception(file.getAbsolutePath())
                    + "\n\n" + ExceptionUtils.getStackTrace(exception);
            module.setError(errorMessage);
        }
        return module;
    }

    /** {@inheritDoc} */
    public MavenModule parse(final InputStream file, final String moduleName) throws InvocationTargetException {
        return null;
    }
}

