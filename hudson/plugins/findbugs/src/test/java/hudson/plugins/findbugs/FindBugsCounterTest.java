package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of findbugs analysis results.
 */
public class FindBugsCounterTest {
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

        assertEquals("Wrong number of classes detected", 6, module.getWarnings("com.avaloq.adt.internal.ui.spell").size());
        assertEquals("Wrong number of classes detected", 2, module.getWarnings("com.avaloq.adt.internal.ui.docu").size());
        JavaProject javaProject = new JavaProject();
        javaProject.addModule(module);
        assertEquals("Wrong number of classes detected", 6, javaProject.getWarnings("com.avaloq.adt.internal.ui.spell").size());
        assertEquals("Wrong number of classes detected", 2, javaProject.getWarnings("com.avaloq.adt.internal.ui.docu").size());

        assertEquals("Wrong number of classes detected", 0, javaProject.getWarnings("wrong.package").size());
    }
}


/* Copyright (c) Avaloq Evolution AG */