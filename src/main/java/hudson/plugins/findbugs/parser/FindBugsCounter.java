package hudson.plugins.findbugs.parser;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.plugins.findbugs.model.JavaProject;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.WorkspaceFile;
import hudson.plugins.findbugs.parser.ant.NativeFindBugsParser;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

/**
 * Counts the number of bugs in a FindBugs analysis file. This parser supports
 * maven-findbugs-plugin and native FindBugs file formats. If the Maven format
 * is detected, then the source filenames are guessed and persisted.
 *
 * @author Ulli Hafner
 */
public class FindBugsCounter {
    /** Associated build. */
    private final AbstractBuild<?, ?> build;

    /**
     * Creates a new instance of <code>FindBugsCounter</code>.
     *
     * @param build
     *            the associated build
     */
    public FindBugsCounter(final AbstractBuild<?, ?> build) {
        this.build = build;
    }

    /**
     * Returns the working directory with the FindBugs results.
     *
     * @return the working directory
     */
    public FilePath getWorkingDirectory() {
        return new FilePath(new java.io.File(build.getRootDir(), "findbugs-results"));
    }

    /**
     * Scans all the FindBugs files in the specified directory and returns the
     * result as a {@link JavaProject}.
     *
     * @return the results
     * @throws IOException if the files could not be read
     * @throws InterruptedException if the operation has been canceled
     * @throws SAXException if the file is not in valid XML format
     * @throws DocumentException
     */
    public JavaProject findBugs() throws IOException, InterruptedException, SAXException, DocumentException {
        MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();
        FilePath[] modules = getWorkingDirectory().list("*.xml");
        JavaProject project = new JavaProject();
        boolean isFormatUndefined = true;
        boolean isMavenFormat = true;
        for (FilePath filePath : modules) {
            if (isFormatUndefined) {
                isMavenFormat = mavenFindBugsParser.accepts(filePath.read());
                isFormatUndefined = false;
            }
            String moduleName = StringUtils.substringBefore(filePath.getName(), ".xml");
            MavenModule module;
            if (isMavenFormat) {
                module = mavenFindBugsParser.parse(filePath.read(), moduleName);
            }
            else {
                NativeFindBugsParser parser = new NativeFindBugsParser();
                module = parser.parse(filePath.read(), moduleName);
            }
            project.addAnnotations(module.getAnnotations());
        }
        if (isMavenFormat) {
            build.getProject().getWorkspace().act(new WorkspaceScanner(project));
        }
        writeMappingFile(project);
        return project;
    }

    /**
     * Writes the actual source file names to a properties file in the build
     * folder.
     *
     * @param project
     *            the project containing the warnings
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if cancel has been pressed during the processing
     */
    private void writeMappingFile(final JavaProject project) throws IOException, InterruptedException {
        Properties mapping = new Properties();
        for (WorkspaceFile file : project.getFiles()) {
            if (file.getName() != null) {
                mapping.setProperty(createFileKey(file), file.getName());
            }
            else {
                Logger.getLogger(WorkspaceScanner.class.getName()).log(Level.WARNING, "NOT Found " + file.getShortName());
            }
        }
        OutputStream mappingStream = getMappingFilePath().write();
        mapping.store(mappingStream, "Mapping of FindBugs warnings to Java files");
        mappingStream.close();
    }

    /**
     * Creates a unique key for a file.
     *
     * @param file
     *            the file to create the key for
     * @return a unique key for a file.
     */
    private String createFileKey(final WorkspaceFile file) {
        return file.getPackageName() + "." + file.getShortName();
    }

    /**
     * Reloads the persisted warning to file mapping.
     *
     * @param project
     *            project containing all the warnings
     * @throws IOException
     *             in case of an file error
     * @throws InterruptedException
     *             if the user presses cancel
     */
    public void restoreMapping(final JavaProject project) throws IOException, InterruptedException {
        FilePath mappingFilePath = getMappingFilePath();
        if (mappingFilePath.exists()) {
            InputStream inputStream = mappingFilePath.read();
            Properties mapping = new Properties();
            mapping.load(inputStream);
            inputStream.close();

            for (WorkspaceFile file : project.getFiles()) {
                String key = createFileKey(file);
                if (mapping.containsKey(key)) {
                    file.setName(mapping.getProperty(key));
                }
            }
        }
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return build.getProject().getLastBuild().number == build.number;
    }

    /**
     * Returns the file path of the mapping file, that maps warnings to actual
     * Java files.
     *
     * @return the file path of the mapping file
     */
    private FilePath getMappingFilePath() {
        return getWorkingDirectory().child("file-mapping.properties");
    }
}