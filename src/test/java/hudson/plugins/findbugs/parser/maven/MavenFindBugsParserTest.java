package hudson.plugins.findbugs.parser.maven;

import static org.junit.Assert.*;
import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;
import hudson.plugins.findbugs.parser.Bug;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class MavenFindBugsParserTest {
    /**
     * FIXME: Document field WRONG_FILE_PROPERTY
     */
    private static final String WRONG_FILE_PROPERTY = "Wrong file property";
    /**
     * FIXME: Document field WRONG_BUG_PROPERTY_SET
     */
    private static final String WRONG_BUG_PROPERTY_SET = "Wrong Bug property set.";
    /** Package of documentation warnings. */
    private static final String DOCU_PACKAGE = "com.avaloq.adt.internal.ui.docu";
    /** Package of spell checker warnings. */
    private static final String SPELL_PACKAGE = "com.avaloq.adt.internal.ui.spell";
    /** Expected number of documentation warnings. */
    private static final int NUMBER_OF_DOCU_WARNINGS = 2;
    /** Expected number of spell checker warnings. */
    private static final int NUMBER_OF_SPELL_WARNINGS = 3;
    /** Error message. */
    private static final String WRONG_WARNINGS_IN_PACKAGE_ERROR = "Wrong number of warnings in a package detected.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";

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
        return new MavenFindBugsParser().parse(MavenFindBugsParserTest.class.getResourceAsStream(fileName), fileName);
    }

    /**
     * Checks whether we correctly detect that the file contains no bugs.
     */
    @Test
    public void scanFileWithNoBugs() throws IOException, SAXException {
        MavenModule module = parseFile("findbugs-no-errors.xml");

        assertEquals(ERROR_MESSAGE, 0, module.getNumberOfAnnotations());
    }

    /**
     * Checks whether we correctly detect an other file.
     */
    @Test(expected = IllegalArgumentException.class)
    public void scanOtherFile() throws IOException, SAXException {
        parseFile("otherfile.xml");
    }

    /**
     * Checks whether we correctly detect all 8 bugs.
     */
    @Test
    public void scanFileWithSomeBugs() throws IOException, SAXException {
        String fileName = "findbugs.xml";
        MavenModule module = parseFile(fileName);

        assertEquals(ERROR_MESSAGE, NUMBER_OF_SPELL_WARNINGS + NUMBER_OF_DOCU_WARNINGS, module.getNumberOfAnnotations());
        assertEquals("Wrong number of packages detected", 2, module.getPackages().size());

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_SPELL_WARNINGS, module.getPackage(SPELL_PACKAGE).getNumberOfAnnotations());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 1, module.getPackage(SPELL_PACKAGE).getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 1, module.getPackage(SPELL_PACKAGE).getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 1, module.getPackage(SPELL_PACKAGE).getNumberOfAnnotations(Priority.LOW));

        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, module.getPackage(DOCU_PACKAGE).getNumberOfAnnotations());
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, NUMBER_OF_DOCU_WARNINGS, module.getPackage(DOCU_PACKAGE).getNumberOfAnnotations(Priority.HIGH));
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, module.getPackage(DOCU_PACKAGE).getNumberOfAnnotations(Priority.NORMAL));
        assertEquals(WRONG_WARNINGS_IN_PACKAGE_ERROR, 0, module.getPackage(DOCU_PACKAGE).getNumberOfAnnotations(Priority.LOW));

        assertEquals("Wrong number of files in package.", 1, module.getPackage(SPELL_PACKAGE).getFiles().size());
        assertEquals("Wrong number of files in package.", 1, module.getPackage(DOCU_PACKAGE).getFiles().size());

        FindBugsMessages.getInstance().initialize();
        FileAnnotation annotation = module.getPackage(SPELL_PACKAGE).getAnnotations(Priority.HIGH).iterator().next();
        assertTrue("Annotation is of wrong type: " + annotation, annotation instanceof Bug);
        Bug bug = (Bug)annotation;
        assertEquals(WRONG_BUG_PROPERTY_SET, "STYLE", bug.getCategory());
        assertEquals(WRONG_BUG_PROPERTY_SET, "PZLA_PREFER_ZERO_LENGTH_ARRAYS", bug.getType());
        assertEquals(WRONG_BUG_PROPERTY_SET, "PZLA: Should com.avaloq.adt.internal.ui.spell.SpellingContentAssistProcessor.computeContextInformation(ITextViewer, int) return a zero length array rather than null?", bug.getMessage());
        assertEquals(WRONG_BUG_PROPERTY_SET, FindBugsMessages.getInstance().getMessage("PZLA_PREFER_ZERO_LENGTH_ARRAYS"), bug.getToolTip());
        assertEquals(WRONG_BUG_PROPERTY_SET, 120, bug.getLineNumber());
        assertTrue(WRONG_BUG_PROPERTY_SET, bug.isLineAnnotation());

        WorkspaceFile file = bug.getWorkspaceFile();
        assertEquals(WRONG_FILE_PROPERTY, fileName, file.getModuleName());
        assertEquals(WRONG_FILE_PROPERTY, SPELL_PACKAGE, file.getPackageName());
        assertEquals(WRONG_FILE_PROPERTY, "SpellingContentAssistProcessor", file.getName());
    }
}


/* Copyright (c) Avaloq Evolution AG */