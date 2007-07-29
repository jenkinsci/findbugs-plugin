package hudson.plugins.findbugs;

import hudson.util.IOException2;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

/**
 * Counts the number of bugs in a FindBugs analysis file.
 *
 * @author Ulli Hafner
 */
public class FindBugsCounter {
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
}