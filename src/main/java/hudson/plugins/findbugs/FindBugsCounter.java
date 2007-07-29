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

            digester.addObjectCreate("BugCollection/file", JavaClass.class);
            digester.addSetProperties("BugCollection/file");
            digester.addSetNext("BugCollection/file", "addClass", JavaClass.class.getName());

            digester.addObjectCreate("BugCollection/file/BugInstance", Warning.class);
            digester.addSetProperties("BugCollection/file/BugInstance");
            digester.addSetNext("BugCollection/file/BugInstance", "addWarning", Warning.class .getName());

            return (Module)digester.parse(file);
        }
        catch (SAXException exception) {
            throw new IOException2(exception);
        }
    }
}