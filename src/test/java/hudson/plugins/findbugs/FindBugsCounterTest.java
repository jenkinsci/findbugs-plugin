package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

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
    private static final String WRONG_CLASSES_ERROR = "Wrong number of classes detected.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";
    /** Expected number of bugs. */
    private static final int NUMBER_OF_BUGS = 8;

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException, SAXException {
        InputStream file = FindBugsCounterTest.class.getResourceAsStream("findbugs-no-errors.xml");
        Module module = new FindBugsCounter(null).parse(file);
        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfWarnings());
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException, SAXException {
        InputStream file = FindBugsCounterTest.class.getResourceAsStream("findbugs.xml");
        Module module = new FindBugsCounter(null).parse(file);
        assertEquals(ERROR_MESSAGE, NUMBER_OF_BUGS, module.getNumberOfWarnings());
        assertEquals("Wrong Version detected", "1.2.0", module.getVersion());
        assertEquals("Wrong number of packages detected", 2, module.getPackages().size());

        assertEquals(WRONG_CLASSES_ERROR, NUMBER_OF_SPELL_WARNINGS, module.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_CLASSES_ERROR, NUMBER_OF_DOCU_WARNINGS, module.getWarnings(DOCU_PACKAGE).size());
        JavaProject javaProject = new JavaProject();
        javaProject.addModule(module);
        assertEquals(WRONG_CLASSES_ERROR, NUMBER_OF_SPELL_WARNINGS, javaProject.getWarnings(SPELL_PACKAGE).size());
        assertEquals(WRONG_CLASSES_ERROR, NUMBER_OF_DOCU_WARNINGS, javaProject.getWarnings(DOCU_PACKAGE).size());

        assertEquals(WRONG_CLASSES_ERROR, 0, javaProject.getWarnings("wrong.package").size());

        Collection<Warning> warnings = javaProject.getWarnings(SPELL_PACKAGE);
        for (Warning warning : warnings) {
            assertEquals("Wrong class found.", "SpellingContentAssistProcessor", warning.getClassname());
        }
    }
}


/* Copyright (c) Avaloq Evolution AG */