package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.findbugs.Messages;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
import hudson.plugins.findbugs.util.ModuleDetector;
import hudson.plugins.findbugs.util.model.JavaProject;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tools.ant.types.FileSet;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

/**
 * Parses the FindBugs files that match the specified pattern and creates a
 * corresponding Java project with a collection of annotations.
 *
 * @author Ulli Hafner
 */
public class FindBugsCollector implements FileCallable<JavaProject> {
    /** Generated ID. */
    private static final long serialVersionUID = -6415863872891783891L;
    /** Logger. */
    private final transient PrintStream logger;
    /** Ant file-set pattern to scan for FindBugs files. */
    private final String filePattern;
    /** Determines whether we need to initialize FindBugs or not. */
    private final boolean autoInitializeFindBugs;

    /**
     * Creates a new instance of <code>FindBugsCollector</code>.
     *
     * @param listener
     *            the Logger
     * @param filePattern
     *            ant file-set pattern to scan for FindBugs files
     * @param autoInitializeFindBugs
     *            determines whether we need to initialize FindBugs or not
     */
    public FindBugsCollector(final PrintStream listener, final String filePattern, final boolean autoInitializeFindBugs) {
        logger = listener;
        this.filePattern = filePattern;
        this.autoInitializeFindBugs = autoInitializeFindBugs;
    }

    /**
     * Logs the specified message.
     *
     * @param message the message
     */
    protected void log(final String message) {
        if (logger != null) {
            logger.println("[FINDBUGS] " + message);
        }
    }

    /** {@inheritDoc} */
    public JavaProject invoke(final File workspace, final VirtualChannel channel) throws IOException {
        String[] findBugsFiles = findFindBugsFiles(workspace);
        JavaProject project = new JavaProject();

        if (findBugsFiles.length == 0) {
            project.setError("No findbugs report files were found. Configuration error?");
            return project;
        }

        try {
            ModuleDetector moduleDetector = new ModuleDetector();
            int duplicateModuleCounter = 1;
            for (String file : findBugsFiles) {
                File findbugsFile = new File(workspace, file);

                String moduleName = moduleDetector.guessModuleName(findbugsFile.getAbsolutePath());
                if (project.containsModule(moduleName)) {
                    moduleName += "-" + duplicateModuleCounter++;
                }
                MavenModule module = new MavenModule(moduleName);

                if (!findbugsFile.canRead()) {
                    String message = Messages.FindBugs_FindBugsCollector_Error_NoPermission(findbugsFile);
                    log(message);
                    module.setError(message);
                    continue;
                }
                if (new FilePath(findbugsFile).length() <= 0) {
                    String message = Messages.FindBugs_FindBugsCollector_Error_EmptyFile(findbugsFile);
                    log(message);
                    module.setError(message);
                    continue;
                }

                module = parseFile(workspace, findbugsFile, module);
                project.addModule(module);
            }
        }
        catch (InterruptedException exception) {
            log("Parsing has been canceled.");
        }
        return project;
    }

    /**
     * Parses the specified FindBugs file and maps all warnings to a
     * corresponding annotation. If the file could not be parsed then an empty
     * module with an error message is returned.
     *
     * @param workspace
     *            the root of the workspace
     * @param findbugsFile
     *            the file to parse
     * @param emptyModule
     *            an empty module with the guessed module name
     * @return the created module
     * @throws InterruptedException
     */
    private MavenModule parseFile(final File workspace, final File findbugsFile, final MavenModule emptyModule) throws InterruptedException {
        Exception exception = null;
        MavenModule module = emptyModule;
        try {
            FilePath filePath = new FilePath(findbugsFile);
            MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();
            if (mavenFindBugsParser.accepts(filePath.read())) {
                log("Activating deprecated FindBugs parser (maven-findbugs-plugin <= 1.1.1)");
                module = mavenFindBugsParser.parse(filePath.read(), emptyModule.getName(), workspace);
                module.setError(Messages.FindBugs_FindBugsCollector_Error_OldMavenPlugin(findbugsFile));
            }
            else {
                log("Activating up-to-date parser (maven-findbugs-plugin >= 1.2 or ant).");
                PlainFindBugsParser parser;
                if (autoInitializeFindBugs) {
                    parser = new NativeFindBugsParser();
                }
                else {
                    parser = new PlainFindBugsParser();
                }
                String moduleRoot = StringUtils.substringBefore(findbugsFile.getPath().replace('\\', '/'), "/target/");
                module = parser.parse(filePath.read(), moduleRoot, emptyModule.getName());
            }
            log("Successfully parsed findbugs file " + findbugsFile + " of module "
                    + module.getName() + " with " + module.getNumberOfAnnotations() + " warnings.");
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
        if (exception != null) {
            String errorMessage = Messages.FindBugs_FindBugsCollector_Error_Exception(findbugsFile)
                    + "\n\n" + ExceptionUtils.getStackTrace(exception);
            log(errorMessage);
            module.setError(errorMessage);
        }
        return module;
    }

    /**
     * Returns an array with the filenames of the FindBugs files that have been
     * found in the workspace.
     *
     * @param workspaceRoot
     *            root directory of the workspace
     * @return the filenames of the FindBugs files
     */
    private String[] findFindBugsFiles(final File workspaceRoot) {
        FileSet fileSet = new FileSet();
        org.apache.tools.ant.Project project = new org.apache.tools.ant.Project();
        fileSet.setProject(project);
        fileSet.setDir(workspaceRoot);
        fileSet.setIncludes(filePattern);

        return fileSet.getDirectoryScanner(project).getIncludedFiles();
    }
}
