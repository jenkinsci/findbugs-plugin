package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class FindBugsCounterTest {
    /** Package of documentation warnings. */
    private static final String DOCU_PACKAGE = "com.avaloq.adt.internal.ui.docu";
    /** Package of spell checker warnings. */
    private static final String SPELL_PACKAGE = "com.avaloq.adt.internal.ui.spell";
    /** Expected number of documentation warnings. */
    private static final int NUMBER_OF_DOCU_WARNINGS = 2;
    /** Expected number of spell checker warnings. */
    private static final int NUMBER_OF_SPELL_WARNINGS = 6;
    /** Error message. */
    private static final String WRONG_WARNINGS_IN_PACKAGE_ERROR = "Wrong number of warnings in a package detected.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";
    /** Expected number of bugs. */
    private static final int NUMBER_OF_BUGS = 8;

    /**
     * Initializes the messages file.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    @BeforeClass
    public static void initialize() throws IOException, SAXException {
        FindBugsMessages.getInstance().initialize();
    }

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException, SAXException {
        Module module = parseFile("findbugs-no-errors.xml");
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
    }

    /**
     * Parses the specified file.
     *
     * @param fileName the file to read
     * @return the parsed module
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    private Module parseFile(final String fileName) throws IOException, SAXException {
        URL file = FindBugsCounterTest.class.getResource(fileName);
        return new FindBugsCounter(null).parse(file);
    }

    /**
     * Checks whether we correctly detect an other file.
     */
    @Test
    public void scanOtherFile() throws IOException, SAXException {
        Module module = parseFile("otherfile.xml");
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
        assertEquals(ERROR_MESSAGE, "Unknown file format", module.getName());
    }

    /**
     * Checks whether we correctly detect a FindBugs 1.2.1 file.
     */
    @Test
    public void scan121File() throws IOException, SAXException {
        Module module = parseFile("findbugs-1.2.1.xml");
        assertEquals(ERROR_MESSAGE, 136, module.getNumberOfWarnings());
        assertEquals("Wrong Version detected", "1.2.1", module.getVersion());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, module.getNumberOfWarnings("java.lang"));

        for (Warning warning : module.getWarnings("org.apache.hadoop.ipc")) {
            assertNotNull("Message should not be empty.", warning.getMessage());
            assertNotNull("Line number should not be empty.", warning.getLineNumber());
        }
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException, SAXException {
        Module module = parseFile("findbugs.xml");
        assertEquals(ERROR_MESSAGE, NUMBER_OF_BUGS, module.getNumberOfWarnings());
        assertEquals("Wrong Version detected", "1.2.0", module.getVersion());
        assertEquals("Wrong number of packages detected", 2, module.getPackages().size());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_SPELL_WARNINGS, module.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, module.getWarnings(DOCU_PACKAGE).size());
        JavaProject javaProject = new JavaProject();
        javaProject.addModule(module);
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_SPELL_WARNINGS, javaProject.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, javaProject.getWarnings(DOCU_PACKAGE).size());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, javaProject.getWarnings("wrong.package").size());

        Collection<Warning> warnings = javaProject.getWarnings(SPELL_PACKAGE);
        for (Warning warning : warnings) {
            assertEquals("Wrong class found.", "SpellingContentAssistProcessor", warning.getClassname());
        }
    }
}


/* Copyright (c) Avaloq Evolution AG */