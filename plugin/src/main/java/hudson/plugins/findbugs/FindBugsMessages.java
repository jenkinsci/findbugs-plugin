package hudson.plugins.findbugs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.plugins.analysis.util.SaxSetup;
import org.apache.commons.digester3.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * Parses the FindBugs pattern descriptions and provides access to these HTML messages.
 *
 * @author Ulli Hafner
 */
// TODO: when there are more translations available we should generalize that approach into a map of maps
public final class FindBugsMessages {
    /** Maps a key to HTML description. */
    private final Map<String, String> messages = new HashMap<String, String>();
    private final Map<String, String> jaMessages = new HashMap<String, String>();
    private final Map<String, String> frMessages = new HashMap<String, String>();
    private final Map<String, String> shortMessages = new HashMap<String, String>();
    private final Map<String, String> jaShortMessages = new HashMap<String, String>();
    private final Map<String, String> frShortMessages = new HashMap<String, String>();

    private static class FindBugsMessagesHolder {
        private static FindBugsMessages INSTANCE = FindBugsMessages.initializeSingleton();
    }

    private static FindBugsMessages initializeSingleton() {
        FindBugsMessages res = new FindBugsMessages();
        SaxSetup sax = new SaxSetup();
        try {
            res.initialize();
        } catch(Exception e) {
            Logger.getLogger(FindBugsMessages.class.getName()).log(Level.WARNING, "FindBugsMessages initializeSingleton failed", e);
        }
        finally {
            sax.cleanup();
        }
        return res;
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static FindBugsMessages getInstance() {
        // lazily created instance, since inner classes are not loaded until they are referenced
        return FindBugsMessagesHolder.INSTANCE;
    }

    /**
     * Initializes the messages map.
     *
     * @throws SAXException
     *             if we can't parse a file
     * @throws IOException
     *             if we can't read a file
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"DE", "REC"})
    private void initialize() throws IOException, SAXException {
        synchronized (messages) {
            loadMessages("messages.xml", messages, shortMessages);
            loadMessages("fb-contrib-messages.xml", messages, shortMessages);
            loadMessages("find-sec-bugs-messages.xml", messages, shortMessages);

            try {
                loadMessages("messages_fr.xml", frMessages, frShortMessages);
                loadMessages("messages_ja.xml", jaMessages, jaShortMessages);
            }
            catch (Exception exception) { // NOCHECKSTYLE
                // ignore failures on localized messages
            }
        }
    }

    private void loadMessages(final String fileName, final Map<String, String> messagesCache, final Map<String, String> shortMessagesCache) throws IOException, SAXException {
        InputStream file = null;
        try {
            file = FindBugsMessages.class.getResourceAsStream(fileName);
            List<Pattern> patterns = parse(file);
            for (Pattern pattern : patterns) {
                messagesCache.put(pattern.getType(), pattern.getDescription());
                shortMessagesCache.put(pattern.getType(), pattern.getShortDescription());
            }
        }
        finally {
            IOUtils.closeQuietly(file);
        }
    }

    /**
     * Parses the FindBugs pattern description.
     *
     * @param file
     *            XML file with the messages
     * @return a list of parsed patterns
     * @throws SAXException
     *             if we can't parse the file
     * @throws IOException
     *             if we can't read the file
     */
    public List<Pattern> parse(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(FindBugsMessages.class.getClassLoader());

        List<Pattern> patterns = new ArrayList<Pattern>();
        digester.push(patterns);

        String startPattern = "*/BugPattern";
        digester.addObjectCreate(startPattern, Pattern.class);
        digester.addSetProperties(startPattern);
        digester.addCallMethod("*/BugPattern/Details", "setDescription", 0);
        digester.addCallMethod("*/BugPattern/ShortDescription", "setShortDescription", 0);
        digester.addSetNext(startPattern, "add");

        digester.parse(file);

        return patterns;
    }

    /**
     * Returns a HTML description for the specified bug.
     *
     * @param name
     *            name of the bug
     * @param locale
     *            the locale of the user
     * @return a HTML description
     */
    public String getMessage(final String name, final Locale locale) {
        String localizedMessage = getLocalizedMessage(name, locale, messages, jaMessages, frMessages);
        return StringUtils.defaultIfEmpty(localizedMessage, Messages.FindBugs_Publisher_NoMessageFoundText());
    }

    /**
     * Returns a short description for the specified bug.
     *
     * @param name
     *            name of the bug
     * @param locale
     *            the locale of the user
     * @return a HTML description for the specified bug.
     */
    public String getShortMessage(final String name, final Locale locale)  {
        String localizedMessage = getLocalizedMessage(name, locale, shortMessages, jaShortMessages, frShortMessages);
        return StringUtils.defaultIfEmpty(localizedMessage, Messages.FindBugs_Publisher_NoMessageFoundText());
    }

    private String getLocalizedMessage(final String name, final Locale locale,
            final Map<String, String> en, final Map<String, String> ja, final Map<String, String> fr) {
        String country = locale.getLanguage();
        String localizedMessage;
        if ("ja".equalsIgnoreCase(country)) {
            localizedMessage = ja.get(name);
        }
        else if ("fr".equalsIgnoreCase(country)) {
            localizedMessage = fr.get(name);
        }
        else {
            localizedMessage = en.get(name);
        }
        return localizedMessage;
    }

    /**
     * Returns the size of this messages cache.
     *
     * @return the number of stored messages (English locale)
     */
    public int size() {
        return messages.size();
    }

    /**
     * Creates a new instance of <code>FindBugsMessages</code>.
     */
    private FindBugsMessages() {
        // prevents instantiation
    }
}

