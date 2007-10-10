package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Tests the class {@link WarningDifferencer}.
 */
public class WarningDifferencerTest {
    /** Corresponding class. */
    private static final String FINDBUGS_CLASS = "findbugs.Class";
    /** String for comparison. */
    private static final String STRING = "type1";
    /** Indicates a wrong calculation of warnings. */
    private static final String WARNINGS_COUNT_ERROR = "Wrong warnings count.";

    /**
     * Checks whether equals works for warnings.
     */
    @Test
    public void testWarningEquals() {
        Warning first = new Warning();
        first.setMessage(STRING);
        first.setQualifiedName(STRING);
        first.setLineNumber(STRING);
        first.setType(STRING);

        Warning second = new Warning();
        second.setMessage(STRING);
        second.setQualifiedName(STRING);
        second.setLineNumber(STRING);
        second.setType(STRING);

        assertEquals("Warnings are not equal.", first, second);
        second.setLineNumber("");
        assertFalse("Warnings are equal.", first.equals(second));
    }

    /**
     * Checks whether differencing detects single changes (new and fixed).
     */
    @Test
    public void testDifferencer() {
        Set<Warning> actual = new HashSet<Warning>();
        Set<Warning> previous = new HashSet<Warning>();

        Warning warning = new Warning();
        warning.setMessage(STRING);
        warning.setLineNumber(STRING);
        warning.setQualifiedName(FINDBUGS_CLASS);
        actual.add(warning);

        warning = new Warning();
        warning.setMessage(STRING);
        warning.setLineNumber(STRING);
        warning.setQualifiedName(FINDBUGS_CLASS);
        previous.add(warning);


        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type2");
        warning.setLineNumber(STRING);
        warning.setQualifiedName(FINDBUGS_CLASS);
        previous.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 1, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type2");
        warning.setLineNumber(STRING);
        warning.setQualifiedName(FINDBUGS_CLASS);
        actual.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type3");
        warning.setLineNumber(STRING);
        warning.setQualifiedName(FINDBUGS_CLASS);
        actual.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 1, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanActualFiles() throws IOException, SAXException {
        Module uiModule = parseFile("ui.xml");
        uiModule.setName("ui");
        Module editor = parseFile("editor.xml");
        editor.setName("editor");
        Module core = parseFile("core.xml");
        core.setName("core");

        JavaProject project = new JavaProject();
        project.addModule(core);
        project.addModule(uiModule);
        project.addModule(editor);

        assertEquals(WARNINGS_COUNT_ERROR, 91, project.getNumberOfWarnings());

        HashSet<Warning> empty = new HashSet<Warning>();
        assertEquals(WARNINGS_COUNT_ERROR, 91, WarningDifferencer.getNewWarnings(project.getWarnings(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(project.getWarnings(), project.getWarnings()).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(empty, project.getWarnings()).size());

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(project.getWarnings(), project.getWarnings()).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(project.getWarnings(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 91, WarningDifferencer.getFixedWarnings(empty, project.getWarnings()).size());
    }

    /**
     * Checks whether we correctly detect priorities.
     */
    @Test
    public void scanWrongCountFile() throws IOException, SAXException {
        Module counter = parseFile("counter.xml");
        counter.setName("counter");

        JavaProject project = new JavaProject();
        project.addModule(counter);

        assertEquals(WARNINGS_COUNT_ERROR, 6 + 26 + 13, counter.getNumberOfWarnings());

        assertEquals(WARNINGS_COUNT_ERROR, 6, WarningDifferencer.countHighPriorityWarnings(counter.getWarnings()));
        assertEquals(WARNINGS_COUNT_ERROR, 26, WarningDifferencer.countNormalPriorityWarnings(counter.getWarnings()));
        assertEquals(WARNINGS_COUNT_ERROR, 13, WarningDifferencer.countLowPriorityWarnings(counter.getWarnings()));
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
        URL file = WarningDifferencerTest.class.getResource(fileName);
        return new FindBugsCounter(null).parse(file);
    }
}

