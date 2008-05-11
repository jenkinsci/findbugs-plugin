package hudson.plugins.findbugs.parser; // NOPMD

import java.net.MalformedURLException;

import edu.umd.cs.findbugs.DetectorFactoryCollection;

/**
 * A parser for the native FindBugs XML files (ant task, batch file or
 * maven-findbugs-plugin >= 1.2-SNAPSHOT). This parser is automatically
 * initialized.
 *
 * @author Ulli Hafner
 */
public class NativeFindBugsParser extends PlainFindBugsParser {
    static {
       initializeFindBugs();
    }

    /**
     * Initializes the FindBugs library.
     */
    private static void initializeFindBugs() {
        try {
            DetectorFactoryCollection.rawInstance().setPluginList(createPluginUrls());
        }
        catch (MalformedURLException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}

