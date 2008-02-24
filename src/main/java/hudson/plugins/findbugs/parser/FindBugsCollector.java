package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.plugins.findbugs.model.JavaProject;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
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
    /** Slash separator on UNIX. */
    private static final String SLASH = "/";
    /** Generated ID. */
    private static final long serialVersionUID = -6415863872891783891L;
    /** Determines whether to skip old files. */
    private static final boolean SKIP_OLD_FILES = false;
    /** Logger. */
    private final transient PrintStream logger;
    /** Build time stamp, only newer files are considered. */
    private final long buildTime;
    /** Ant file-set pattern to scan for FindBugs files. */
    private final String filePattern;

    /**
     * Creates a new instance of <code>FindBugsCollector</code>.
     *
     * @param listener
     *            the Logger
     * @param buildTime
     *            build time stamp, only newer files are considered
     * @param filePattern
     *            ant file-set pattern to scan for FindBugs files
     */
    public FindBugsCollector(final PrintStream listener, final long buildTime, final String filePattern) {
        logger = listener;
        this.buildTime = buildTime;
        this.filePattern = filePattern;
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
            for (String file : findBugsFiles) {
                File findbugsFile = new File(workspace, file);

                String moduleName = guessModuleName(findbugsFile.getAbsolutePath());
                MavenModule module = new MavenModule(moduleName);

                if (SKIP_OLD_FILES && findbugsFile.lastModified() < buildTime) {
                    String message = "Skipping " + findbugsFile + " because it's not up to date";
                    logger.println(message);
                    module.setError(message);
                    continue;
                }
                if (!findbugsFile.canRead()) {
                    String message = "Skipping " + findbugsFile + " because we have no permission to read the file.";
                    logger.println(message);
                    module.setError(message);
                    continue;
                }

                module = parseFile(workspace, findbugsFile, module);
                project.addModule(module);
            }
        }
        catch (InterruptedException exception) {
            logger.println("Parsing has been canceled.");
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
                logger.println("Activating parser for maven-findbugs-plugin <= 1.1.1.");
                module = mavenFindBugsParser.parse(filePath.read(), emptyModule.getName(), workspace);
                module.setError("FindBugs file " + findbugsFile + " was created with the outdated version 1.1.1 of the maven-findbugs-plugin." +
                        " Please upgrade to the 1.2 version.");
            }
            else {
                logger.println("Activating parser for findbugs ant task, batch script, or maven-findbugs-plugin > 1.1.1.");
                NativeFindBugsParser parser = new NativeFindBugsParser();
                String moduleRoot = StringUtils.substringBefore(findbugsFile.getPath().replace('\\', '/'), "/target/");
                module = parser.parse(filePath.read(), moduleRoot, emptyModule.getName());
            }
            logger.println("Successfully parsed findbugs file " + findbugsFile + " of module "
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
            String errorMessage = "Parsing of file " + findbugsFile + " failed due to an exception:\n\n"
                    + ExceptionUtils.getStackTrace(exception);
            logger.println(errorMessage);
            module.setError(errorMessage);
        }
        return module;
    }

    /**
     * Guesses the module name based on the specified file name. Actually works
     * only for maven projects.
     *
     * @param fileName
     *            the filename to guess the module name from
     * @return the module name
     */
    public static String guessModuleName(final String fileName) {
        String separator;
        if (fileName.contains(SLASH)) {
            separator = SLASH;
        }
        else {
            separator = "\\";
        }
        String path = StringUtils.substringBefore(fileName, separator + "target");
        if (fileName.equals(path)) {
            return "";
        }
        if (path.contains(separator)) {
            return StringUtils.substringAfterLast(path, separator);
        }
        else {
            return path;
        }
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