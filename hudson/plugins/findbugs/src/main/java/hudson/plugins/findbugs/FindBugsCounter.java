package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Counts the number of bugs in a FindBugs analysis file.
 *
 * @author Ulli Hafner
 */
public class FindBugsCounter {
    /** Parent XPATH element. */
    private static final String BUG_COLLECTION_XPATH = "BugCollection";
    /** Associated build. */
    private final Build<?, ?> build;

    /**
     * Creates a new instance of <code>FindBugsCounter</code>.
     * @param build the associated build
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
        boolean use120Scanner = isIn120Format(file.openStream());
        if (use120Scanner) {
            return parse120Format(file.openStream());
        }
        else {
            return parse121Format(file.openStream());
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
        boolean use120Scanner = isIn120Format(file.read());
        if (use120Scanner) {
            return parse120Format(file.read());
        }
        else {
            return parse121Format(file.read());
        }
    }

    /**
     * Returns the parsed FindBugs analysis file. The used scanner is 1.2.0
     * format.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private Module parse120Format(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        digester.addObjectCreate(BUG_COLLECTION_XPATH, Module.class);
        digester.addSetProperties(BUG_COLLECTION_XPATH);

        String classXpath = "BugCollection/file";
        digester.addObjectCreate(classXpath, JavaClass.class);
        digester.addSetProperties(classXpath);
        digester.addSetNext(classXpath, "addClass", JavaClass.class.getName());

        String warningXpath = "BugCollection/file/BugInstance";
        digester.addObjectCreate(warningXpath, Warning.class);
        digester.addSetProperties(warningXpath);
        digester.addSetNext(warningXpath, "addWarning", Warning.class.getName());

        return (Module)ObjectUtils.defaultIfNull(digester.parse(file), new Module("Unknown file format"));
    }

    /**
     * Returns the parsed FindBugs analysis file. The used scanner is 1.2.0
     * format.
     *
     * @param file
     *            the FindBugs analysis file
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private Module parse121Format(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        digester.addObjectCreate(BUG_COLLECTION_XPATH, Module.class);
        digester.addSetProperties(BUG_COLLECTION_XPATH);

        digester.addObjectCreate("BugCollection/BugInstance", Warning.class);
        digester.addSetProperties("BugCollection/BugInstance");
        digester.addSetNext("BugCollection/BugInstance", "addWarning", Warning.class.getName());
        digester.addCallMethod("BugCollection/BugInstance/LongMessage", "setMessage", 0);

        digester.addObjectCreate("BugCollection/BugInstance/Class", JavaClass.class);
        digester.addSetProperties("BugCollection/BugInstance/Class");
        digester.addSetNext("BugCollection/BugInstance/Class", "linkClass", JavaClass.class.getName());

        return (Module)ObjectUtils.defaultIfNull(digester.parse(file), new Module("Unknown file format"));
    }

    /**
     * Returns whether the provided file is in FindBugs 1.2.0 format.
     *
     * @param file
     *            the file to check
     * @return <code>true</code> if the provided file is in FindBugs 1.2.0
     *         format.
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    private boolean isIn120Format(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsCounter.class.getClassLoader());

        digester.addObjectCreate(BUG_COLLECTION_XPATH, Module.class);
        digester.addSetProperties(BUG_COLLECTION_XPATH);

        Module module = (Module)digester.parse(file);

        return module != null && "1.2.0".equals(module.getVersion());
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
        if (isCurrent()) {
            build.getProject().getWorkspace().act(new WorkspaceScanner(project));
        }
        return project;
    }

    /**
     * Returns whether this result belongs to the last build.
     *
     * @return <code>true</code> if this result belongs to the last build
     */
    public boolean isCurrent() {
        return build.getProject().getLastBuild().number == build.number;
    }
}