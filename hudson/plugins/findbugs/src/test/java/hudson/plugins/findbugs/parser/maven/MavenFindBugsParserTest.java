package hudson.plugins.findbugs.parser.maven;

import static org.junit.Assert.*;
import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.parser.Bug;
import hudson.plugins.findbugs.parser.NativeFindBugsParser;
import hudson.plugins.findbugs.util.model.AbstractAnnotation;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.LineRange;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.plugins.findbugs.util.model.WorkspaceFile;

import java.io.IOException;
import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class MavenFindBugsParserTest {
    /** Class name of string. */
    private static final String STRING_CLASS = "String";
    /** Class name of inner class. */
    private static final String STRING_INNER_CLASS = "String$InnerClass1";
    /** Windows Filename to String class. */
    private static final String STRING_FILE = "C:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.ui\\src\\main\\java\\java\\lang\\String.java";
    /** Windows Filename to String class in tests folder. */
    private static final String STRING_TEST_FILE = "C:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.ui\\src\\test\\java\\java\\lang\\String.java";
    /** Windows Filename to String class in plain source folder. */
    private static final String STRING_SRC_FILE = "C:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.ui\\src\\java\\lang\\String.java";
    /** Unix Filename to String class. */
    private static final String STRING_FILE_UNIX = STRING_FILE.replace("\\", "/");
    /** Filename to Integer class. */
    private static final String INTEGER_FILE = "C:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.ui\\src\\main\\java\\java\\lang\\Integer.java";
    /** Package of documentation warnings. */
    private static final String DOCU_PACKAGE = "com.avaloq.adt.internal.ui.docu";
    /** Package of spell checker warnings. */
    private static final String SPELL_PACKAGE = "com.avaloq.adt.internal.ui.spell";
    /** Expected number of documentation warnings. */
    private static final int NUMBER_OF_DOCU_WARNINGS = 2;
    /** Expected number of spell checker warnings. */
    private static final int NUMBER_OF_SPELL_WARNINGS = 3;
    /** Error message. */
    private static final String WRONG_FILENAME_GUESSED = "Wrong filename guessed.";
    /** Error message. */
    private static final String WRONG_FILE_PROPERTY = "Wrong file property";
    /** Error message. */
    private static final String WRONG_BUG_PROPERTY_SET = "Wrong Bug property set.";
    /** Error message. */
    private static final String WRONG_WARNINGS_IN_PACKAGE_ERROR = "Wrong number of warnings in a package detected.";
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";
    /** Parser under test. */
    private final MavenFindBugsParser mavenFindBugsParser = new MavenFindBugsParser();

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
        return mavenFindBugsParser.parse(MavenFindBugsParserTest.class.getResourceAsStream(fileName), fileName);
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
    @Test(expected = SAXException.class)
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

        Collection<LineRange> lineRanges = bug.getLineRanges();
        assertEquals(WRONG_BUG_PROPERTY_SET, 1, lineRanges.size());
        LineRange range = lineRanges.iterator().next();
        assertEquals(WRONG_BUG_PROPERTY_SET, 120, range.getStart());
        assertEquals(WRONG_BUG_PROPERTY_SET, 120, range.getEnd());
        assertEquals(WRONG_FILE_PROPERTY, fileName, bug.getModuleName());
        assertEquals(WRONG_FILE_PROPERTY, SPELL_PACKAGE, bug.getPackageName());
        assertEquals(WRONG_FILE_PROPERTY, "SpellingContentAssistProcessor", bug.getFileName());
    }


    /**
     * Checks whether we correctly detect a maven FindBugs file.
     *
     * @throws SAXException
     *             in case of an error
     * @throws IOException
     *             in case of an error
     */
    @Test
    public void testWorkspaceFileNames() throws IOException, SAXException {
        String fileName = "findbugs-classname.xml";
        MavenModule module = parseFile(fileName);

        Collection<WorkspaceFile> files = module.getFiles();
        Assert.assertEquals("Wrong number of files.", 1, files.size());
        Assert.assertEquals("Wrong number of bugs.", 7, module.getNumberOfAnnotations());

        for (FileAnnotation annotation : module.getAnnotations()) {
            Assert.assertEquals("Wrong file name in annotation.", "ChangeDBCore", annotation.getFileName());
        }
    }

    /**
     * Checks whether we correctly detect a maven FindBugs file.
     */
    @Test
    public void detectMavenFile() {
        Assert.assertTrue(mavenFindBugsParser.accepts(MavenFindBugsParser.class.getResourceAsStream("findbugs.xml")));
    }

    /**
     * Checks whether we correctly detect a native FindBugs file.
     */
    @Test
    public void detectNativeFile() {
        Assert.assertFalse(mavenFindBugsParser.accepts(NativeFindBugsParser.class.getResourceAsStream("findbugs-native.xml")));
    }

    /**
     * Checks whether we correctly detect a native FindBugs file.
     */
    @Test
    public void detectInvalidFile() {
        Assert.assertFalse(mavenFindBugsParser.accepts(NativeFindBugsParser.class.getResourceAsStream("noxml.txt")));
    }

    /**
     * Checks whether we correctly map a class to a Windows file.
     */
    @Test
    public void checkSourceFileMappingWindowsFile() {
        String[] javaFiles = new String[] {INTEGER_FILE, STRING_FILE};

        AbstractAnnotation bug = runClassMapper(STRING_CLASS, javaFiles);

        Assert.assertEquals(STRING_FILE_UNIX, bug.getFileName());
    }

    /**
     * Checks whether we correctly map a class to a Windows file in the tests folder.
     */
    @Test
    public void checkSourceFileMappingWindowsFileInTestsFolder() {
        String[] javaFiles = new String[] {INTEGER_FILE, STRING_TEST_FILE};

        AbstractAnnotation bug = runClassMapper(STRING_CLASS, javaFiles);

        Assert.assertEquals(STRING_TEST_FILE.replace("\\", "/"), bug.getFileName());
    }

    /**
     * Checks whether we correctly map a class to a Windows file in a src folder.
     */
    @Test
    public void checkSourceFileMappingWindowsFileInPlainSrcFolder() {
        String[] javaFiles = new String[] {INTEGER_FILE, STRING_SRC_FILE};

        AbstractAnnotation bug = runClassMapper(STRING_CLASS, javaFiles);

        Assert.assertEquals(STRING_SRC_FILE.replace("\\", "/"), bug.getFileName());
    }

    /**
     * Checks whether we correctly map an inner class to a Windows file.
     */
    @Test
    public void checkInnerClassMapping() {
        String[] javaFiles = new String[] {INTEGER_FILE, STRING_FILE};

        AbstractAnnotation bug = runClassMapper(STRING_INNER_CLASS, javaFiles);

        Assert.assertEquals(STRING_FILE_UNIX, bug.getFileName());
    }

    /**
     * Checks whether we correctly map a class to an Unix file.
     */
    @Test
    public void checkSourceFileMappingUnixFile() {
        String[] javaFiles = new String[] {INTEGER_FILE, STRING_FILE_UNIX};

        AbstractAnnotation bug = runClassMapper(STRING_CLASS, javaFiles);

        Assert.assertEquals(WRONG_FILENAME_GUESSED, STRING_FILE_UNIX, bug.getFileName());
    }

    /**
     * Creates a simple bug and calls the class mapper.
     *
     * @param className
     *            name of the class with the bug
     * @param javaFiles
     *            the filenames
     * @return the simple bug
     */
    private AbstractAnnotation runClassMapper(final String className, final String[] javaFiles) {
        Bug bug = new Bug(Priority.HIGH, "", "", "");
        bug.setPackageName("java.lang");
        bug.setFileName(className);

        MavenModule module = new MavenModule();
        module.addAnnotation(bug);

        mavenFindBugsParser.mapFiles(module, javaFiles);
        return bug;
    }
}


/* Copyright (c) Avaloq Evolution AG */