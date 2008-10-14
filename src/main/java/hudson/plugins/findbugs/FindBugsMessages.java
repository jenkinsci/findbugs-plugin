package hudson.plugins.findbugs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Parses the FindBugs pattern descriptions and provides access to these HTML messages.
 *
 * @author Ulli Hafner
 */
public final class FindBugsMessages {
    /** Maps a key to HTML description. */
    private final Map<String, String> messages = new HashMap<String, String>();
    /** Maps a key to HTML description. */
    private final Map<String, String> shortMessages = new HashMap<String, String>();
    /** Singleton instance. */
    private static final FindBugsMessages INSTANCE = new FindBugsMessages();

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static FindBugsMessages getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the messages map.
     *
     * @throws SAXException
     *             if we can't parse a file
     * @throws IOException
     *             if we can't read a file
     */
    public synchronized void initialize() throws IOException, SAXException {
        loadMessages("messages.xml");
        loadMessages("fb-contrib-messages.xml");
    }

    /**
     * Loads the message file and adds all messages to the mapping.
     *
     * @param fileName the file to load
     * @throws SAXException
     *             if we can't parse the file
     * @throws IOException
     *             if we can't read the file
     */
    private void loadMessages(final String fileName) throws IOException, SAXException {
        InputStream file = FindBugsMessages.class.getResourceAsStream(fileName);
        List<Pattern> patterns = parse(file);
        for (Pattern pattern : patterns) {
            messages.put(pattern.getType(), pattern.getDescription());
            shortMessages.put(pattern.getType(), pattern.getShortDescription());
        }
    }

    /**
     * Parses the FindBugs pattern description.
     *
     * @param file
     *            XML file with the messages
     * @return a list of parsed patterns
     * @throws SAXException if we can't parse the file
     * @throws IOException if we can't read the file
     */
    public List<Pattern> parse(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsMessages.class.getClassLoader());

        List<Pattern> patterns = new ArrayList<Pattern>();
        digester.push(patterns);

        digester.addObjectCreate("*/BugPattern", Pattern.class);
        digester.addSetProperties("*/BugPattern");
        digester.addCallMethod("*/BugPattern/Details", "setDescription", 0);
        digester.addCallMethod("*/BugPattern/ShortDescription", "setShortDescription", 0);
        digester.addSetNext("*/BugPattern", "add");

        digester.parse(file);

        return patterns;
    }

    /**
     * Returns a HTML description for the specified bug.
     *
     * @param name
     *            name of the bug
     * @return a HTML description for the specified bug.
     */
    public String getMessage(final String name)  {
        return StringUtils.defaultIfEmpty(messages.get(name), Messages.FindBugs_Publisher_NoMessageFoundText());
    }

    /**
     * Returns a short description for the specified bug.
     *
     * @param name
     *            name of the bug
     * @return a HTML description for the specified bug.
     */
    public String getShortMessage(final String name)  {
        return StringUtils.defaultIfEmpty(shortMessages.get(name), Messages.FindBugs_Publisher_NoMessageFoundText());
    }

    /**
     * Creates a new instance of <code>FindBugsMessages</code>.
     */
    private FindBugsMessages() {
        // prevents instantiation
    }
}

