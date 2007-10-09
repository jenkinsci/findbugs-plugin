package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
    private final Build<?, ?> build;

    /**
     * Creates a new instance of <code>FindBugsCounter</code>.
     *
     * @param build
     *            the associated build
     */
    public FindBugsCounter(final Build<?, ?> build) {
        this.build = build;
    }

    /**
     * Returns the parsed FindBugs analysis file.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException if the file is not in valid XML format
     */
    public Module parse(final URL file) throws IOException, SAXException {
        if (isMavenReport(file.openStream())) {
            return parseMavenFormat(file.openStream());
        }
        else {
            return parseNativeFormat(file.openStream());
        }
    }

    /**
     * Returns the parsed FindBugs analysis file.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException if the file is not in valid XML format
     */
    public Module parse(final FilePath file) throws IOException, SAXException {
        if (isMavenReport(file.read())) {
            return parseMavenFormat(file.read());
        }
        else {
            return parseNativeFormat(file.read());
        }
    }


    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the Maven FindBugs plug-in format.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private Module parseMavenFormat(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        String root = "BugCollection";
        digester.addObjectCreate(root, Module.class);
        digester.addSetProperties(root);

        String classXpath = "BugCollection/file";
        digester.addObjectCreate(classXpath, JavaClass.class);
        digester.addSetProperties(classXpath);
        digester.addSetNext(classXpath, "addClass", JavaClass.class.getName());

        String warningXpath = "BugCollection/file/BugInstance";
        digester.addObjectCreate(warningXpath, Warning.class);
        digester.addSetProperties(warningXpath);
        digester.addSetNext(warningXpath, "addWarning", Warning.class.getName());

        Module module = (Module)ObjectUtils.defaultIfNull(digester.parse(file), new Module("Unknown file format"));
        module.setMavenFormat(true);

        return module;
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in the native
     * FinBugs format.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private Module parseNativeFormat(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        digester.addObjectCreate("BugCollection", Module.class);
        digester.addSetProperties("BugCollection");

        digester.addObjectCreate("BugCollection/Project", ProjectInformation.class);
        digester.addSetNext("BugCollection/Project", "setProjectInformation", ProjectInformation.class.getName());
        digester.addCallMethod("BugCollection/Project/SrcDir", "addSourcePath", 0);

        digester.addObjectCreate("BugCollection/BugInstance", Warning.class);
        digester.addSetProperties("BugCollection/BugInstance");
        digester.addSetNext("BugCollection/BugInstance", "addWarning", Warning.class.getName());
        digester.addCallMethod("BugCollection/BugInstance/LongMessage", "setMessage", 0);

        digester.addObjectCreate("BugCollection/BugInstance/Class", JavaClass.class);
        digester.addSetProperties("BugCollection/BugInstance/Class");

        digester.addObjectCreate("BugCollection/BugInstance/Class/SourceLine", SourceLine.class);
        digester.addSetProperties("BugCollection/BugInstance/Class/SourceLine");
        digester.addSetNext("BugCollection/BugInstance/Class/SourceLine", "addSourceLine", SourceLine.class.getName());

        digester.addSetNext("BugCollection/BugInstance/Class", "linkClass", JavaClass.class.getName());

        Module module = (Module)ObjectUtils.defaultIfNull(digester.parse(file), new Module("Unknown file format"));
        module.setMavenFormat(false);

        Set<String> paths = module.getProjectInformation().getSourcePaths();
        if (paths.size() == 1) {
            String prefix = paths.iterator().next();
            for (Warning warning : module.getWarnings()) {
                warning.setFile(prefix + "/" + warning.getFile());
            }
        }
        return module;
    }

    /**
     * Checks whether the provided file is in FindBugs native or Maven plug-in
     * format. A file is considered in the Maven format, if it contains the
     * XPath "BugCollection/file/BugInstance".
     *
     * @param file
     *            the file to check
     * @return <code>true</code> if the provided file is in maven format.
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private boolean isMavenReport(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        digester.addObjectCreate("BugCollection/file/BugInstance", Module.class);

        Module module = (Module)digester.parse(file);

        return module != null;
    }

    /**
     * Returns the working directory with the FindBugs results.
     *
     * @return the working directory
     */
    public FilePath getWorkingDirectory() {
        return new FilePath(new File(build.getRootDir(), "findbugs-results"));
    }

    /**
     * Scans all the FindBugs files in the specified directory and returns the
     * result as a {@link JavaProject}.
     *
     * @return the results
     * @throws IOException if the files could not be read
     * @throws InterruptedException if the operation has been canceled
     * @throws SAXException if the file is not in valid XML format
     */
    public JavaProject findBugs() throws IOException, InterruptedException, SAXException {
        FilePath[] list = getWorkingDirectory().list("*.xml");
        JavaProject project = new JavaProject();
        for (FilePath filePath : list) {
            Module module = parse(filePath);
            module.setName(StringUtils.substringBefore(filePath.getName(), ".xml"));
            project.addModule(module);
        }
        return project;
    }

    /**
     * Creates a mapping of warnings to actual workspace files.
     *
     * @param project
     *            project containing all the warnings
     * @throws IOException
     *             in case of an IO error
     * @throws InterruptedException
     *             if cancel has been pressed during the processing
     */
    public void mapWarnings2Files(final JavaProject project) throws IOException, InterruptedException {
        if (project.isMavenFormat()) {
            build.getProject().getWorkspace().act(new WorkspaceScanner(project));
        }
        else {
            mapNativeWarnings2Files(project);
        }
        writeMappingFile(project);
    }

    /**
     * Creates a mapping of warnings to actual workspace files (for native format). The result is
     * persisted in the results folder.
     *
     * @param project
     *            project containing all the warnings
     */
    private void mapNativeWarnings2Files(final JavaProject project) {
        for (Module module : project.getModules()) {
            Set<String> paths = module.getProjectInformation().getSourcePaths();
            if (paths.size() > 1) {
                for (Warning warning : module.getWarnings()) {
                    for (String path : paths) {
                        String actualPath = path + "/" + warning.getFile();
                        File file = new File(actualPath.replace('!', '/'));
                        if (file.exists()) {
                            warning.setFile(actualPath);
                        }
                    }
                }
            }
        }
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
        for (Warning warning : project.getWarnings()) {
            if (warning.getFile() != null) {
                mapping.setProperty(warning.getQualifiedName(), warning.getFile());
            }
        }
        OutputStream mappingStream = getMappingFilePath().write();
        mapping.store(mappingStream, "Mapping of FindBugs warnings to Java files");
        mappingStream.close();
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

            for (Warning warning : project.getWarnings()) {
                String key = warning.getQualifiedName();
                if (mapping.containsKey(key)) {
                    warning.setFile(mapping.getProperty(key));
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