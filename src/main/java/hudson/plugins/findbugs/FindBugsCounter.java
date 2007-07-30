package hudson.plugins.findbugs;

import hudson.FilePath;
import hudson.model.Build;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Counts the number of bugs in a FindBugs analysis file.
 *
 * @author Ulli Hafner
 */
public class FindBugsCounter {
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
     */
    protected Module parse(final InputStream file) throws IOException {
        try {
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(FindBugsCounter.class.getClassLoader());

            digester.addObjectCreate("BugCollection", Module.class);
            digester.addSetProperties("BugCollection");

            String classXpath = "BugCollection/file";
            digester.addObjectCreate(classXpath, JavaClass.class);
            digester.addSetProperties(classXpath);
            digester.addSetNext(classXpath, "addClass", JavaClass.class.getName());

            String warningXpath = "BugCollection/file/BugInstance";
            digester.addObjectCreate(warningXpath, Warning.class);
            digester.addSetProperties(warningXpath);
            digester.addSetNext(warningXpath, "addWarning", Warning.class .getName());

            return (Module)digester.parse(file);
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
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
     * Scans all the FibndBugs files in the specified directory and returns the
     * result as a {@link JavaProject}.
     *
     * @return the results
     * @throws IOException if the files could not be read
     * @throws InterruptedException if the operation has been canceled
     */
    public JavaProject findBugs() throws IOException, InterruptedException {
        FilePath[] list = getWorkingDirectory().list("*.xml");
        JavaProject project = new JavaProject();
        for (FilePath filePath : list) {
            Module module = parse(filePath.read());
            module.setName(StringUtils.substringBefore(filePath.getName(), ".xml"));
            project.addModule(module);
        }
        return project;
    }
}