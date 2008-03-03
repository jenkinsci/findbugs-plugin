package hudson.plugins.findbugs;

import static org.junit.Assert.*;
import hudson.plugins.findbugs.parser.Bug;
import hudson.plugins.findbugs.parser.maven.MavenFindBugsParser;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.JavaProject;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.plugins.findbugs.util.model.Priority;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Tests the class {@link WarningDifferencer}.
 */
@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public class WarningDifferencerTest {
    /** String for comparison. */
    private static final String STRING = "type1";
    /** Indicates a wrong calculation of warnings. */
    private static final String WARNINGS_COUNT_ERROR = "Wrong warnings count.";

    /**
     * Checks whether equals works for warnings.
     */
    @Test
    public void testWarningEquals() {
        Bug first  = new Bug(Priority.HIGH, STRING, STRING, STRING, 2);
        Bug second = new Bug(Priority.HIGH, STRING, STRING, STRING, 2);

        assertEquals("Warnings are not equal.", first, second);

        first.setFileName(STRING);

        assertFalse("Warnings are equal.", first.equals(second));
    }

    /**
     * Checks whether differencing detects single changes (new and fixed).
     */
    @Test
    public void testDifferencer() {
        Set<FileAnnotation> actual = new HashSet<FileAnnotation>();
        Set<FileAnnotation> previous = new HashSet<FileAnnotation>();

        Bug warning = new Bug(Priority.HIGH, STRING, STRING, STRING, 2);
        actual.add(warning);

        warning = new Bug(Priority.HIGH, STRING, STRING, STRING, 2);
        previous.add(warning);


        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Bug(Priority.HIGH, "type2", STRING, STRING, 2);
        previous.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 1, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Bug(Priority.HIGH, "type2", STRING, STRING, 2);
        actual.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Bug(Priority.HIGH, "type3", STRING, STRING, 2);
        actual.add(warning);

        assertEquals(WARNINGS_COUNT_ERROR, 1, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(actual, previous).size());
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanActualFiles() throws Exception {
        MavenModule uiModule = parseFile("ui.xml");
        uiModule.setName("ui");
        MavenModule editor = parseFile("editor.xml");
        editor.setName("editor");
        MavenModule core = parseFile("core.xml");
        core.setName("core");

        JavaProject project = new JavaProject();
        project.addAnnotations(core.getAnnotations());
        project.addAnnotations(uiModule.getAnnotations());
        project.addAnnotations(editor.getAnnotations());

        assertEquals(WARNINGS_COUNT_ERROR, 9, core.getNumberOfAnnotations());
        assertEquals(WARNINGS_COUNT_ERROR, 55, uiModule.getNumberOfAnnotations());
        assertEquals(WARNINGS_COUNT_ERROR, 27, editor.getNumberOfAnnotations());
        assertEquals(WARNINGS_COUNT_ERROR, 91, project.getNumberOfAnnotations());
        assertEquals(WARNINGS_COUNT_ERROR, 91, project.getAnnotations().size());

        Set<FileAnnotation> empty = new HashSet<FileAnnotation>();
        assertEquals(WARNINGS_COUNT_ERROR, 9, WarningDifferencer.getNewWarnings(core.getAnnotations(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 55, WarningDifferencer.getNewWarnings(uiModule.getAnnotations(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 27, WarningDifferencer.getNewWarnings(editor.getAnnotations(), empty).size());

        project = new JavaProject();
        project.addAnnotations(core.getAnnotations());
        project.addAnnotations(uiModule.getAnnotations());
        project.addAnnotations(editor.getAnnotations());

        assertEquals(WARNINGS_COUNT_ERROR, 91, WarningDifferencer.getNewWarnings(project.getAnnotations(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(project.getAnnotations(), project.getAnnotations()).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getNewWarnings(empty, project.getAnnotations()).size());

        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(project.getAnnotations(), project.getAnnotations()).size());
        assertEquals(WARNINGS_COUNT_ERROR, 0, WarningDifferencer.getFixedWarnings(project.getAnnotations(), empty).size());
        assertEquals(WARNINGS_COUNT_ERROR, 91, WarningDifferencer.getFixedWarnings(empty, project.getAnnotations()).size());
    }

    /**
     * Checks whether we correctly detect priorities.
     */
    @Test
    public void scanWrongCountFile() throws Exception {
        MavenModule counter = parseFile("counter.xml");
        counter.setName("counter");

        assertEquals(WARNINGS_COUNT_ERROR, 6 + 26 + 13, counter.getNumberOfAnnotations());
        assertEquals(WARNINGS_COUNT_ERROR, 6, counter.getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WARNINGS_COUNT_ERROR, 26, counter.getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WARNINGS_COUNT_ERROR, 13, counter.getNumberOfAnnotations(Priority.LOW));
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
    private MavenModule parseFile(final String fileName) throws IOException, SAXException {
        return new MavenFindBugsParser().parse(WarningDifferencerTest.class.getResourceAsStream(fileName), fileName);
    }
}

