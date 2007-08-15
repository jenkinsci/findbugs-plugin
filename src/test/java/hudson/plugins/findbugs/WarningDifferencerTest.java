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
    /**
     * Checks whether equals works for warnings.
     */
    @Test
    public void testWarningEquals() {
        Warning first = new Warning();
        first.setMessage("type1");
        first.setQualifiedName("type1");
        first.setLineNumber("type1");
        first.setType("type1");

        Warning second = new Warning();
        second.setMessage("type1");
        second.setQualifiedName("type1");
        second.setLineNumber("type1");
        second.setType("type1");

        assertEquals(first, second);
        second.setLineNumber("");
        assertFalse(first.equals(second));
    }

    /**
     * Checks whether differencing detects single changes (new and fixed).
     */
    @Test
    public void testDifferencer() {
        Set<Warning> actual = new HashSet<Warning>();
        Set<Warning> previous = new HashSet<Warning>();

        Warning warning = new Warning();
        warning.setMessage("type1");
        warning.setLineNumber("type1");
        warning.setQualifiedName("findbugs.Class");
        actual.add(warning);

        warning = new Warning();
        warning.setMessage("type1");
        warning.setLineNumber("type1");
        warning.setQualifiedName("findbugs.Class");
        previous.add(warning);


        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type2");
        warning.setLineNumber("type1");
        warning.setQualifiedName("findbugs.Class");
        previous.add(warning);

        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(1, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type2");
        warning.setLineNumber("type1");
        warning.setQualifiedName("findbugs.Class");
        actual.add(warning);

        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setMessage("type3");
        warning.setLineNumber("type1");
        warning.setQualifiedName("findbugs.Class");
        actual.add(warning);

        assertEquals(1, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanActualFiles() throws IOException, SAXException {
        Module ui = parseFile("ui.xml");
        ui.setName("ui");
        Module editor = parseFile("editor.xml");
        editor.setName("editor");
        Module core = parseFile("core.xml");
        core.setName("core");

        JavaProject project = new JavaProject();
        project.addModule(core);
        project.addModule(ui);
        project.addModule(editor);

        assertEquals(91, project.getNumberOfWarnings());

        HashSet<Warning> empty = new HashSet<Warning>();
        assertEquals(91, WarningDifferencer.getNewWarnings(project.getWarnings(), empty).size());
        assertEquals(0, WarningDifferencer.getNewWarnings(project.getWarnings(), project.getWarnings()).size());
        assertEquals(0, WarningDifferencer.getNewWarnings(empty, project.getWarnings()).size());

        assertEquals(0, WarningDifferencer.getFixedWarnings(project.getWarnings(), project.getWarnings()).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(project.getWarnings(), empty).size());
        assertEquals(91, WarningDifferencer.getFixedWarnings(empty, project.getWarnings()).size());
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

        assertEquals(6 + 26 + 13, counter.getNumberOfWarnings());

        assertEquals(6, WarningDifferencer.countHighPriorityWarnings(counter.getWarnings()));
        assertEquals(26, WarningDifferencer.countNormalPriorityWarnings(counter.getWarnings()));
        assertEquals(13, WarningDifferencer.countLowPriorityWarnings(counter.getWarnings()));
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

