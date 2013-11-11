package hudson.plugins.findbugs.parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.junit.Test;
import org.xml.sax.SAXException;

import hudson.plugins.analysis.test.AbstractEnglishLocaleTest;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.JavaPackage;
import hudson.plugins.analysis.util.model.LineRange;
import hudson.plugins.analysis.util.model.MavenModule;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.Messages;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class FindBugsParserTest extends AbstractEnglishLocaleTest {
    /** Error message. */
    private static final String WRONG_MESSAGE_PARSED = "Wrong message parsed.";
    /** Error message. */
    private static final String BUG_WITH_GIVEN_HASHCODE_NOT_FOUND = "Bug with given hashcode not found";
    /** Hash code of second warning. */
    private static final String SECOND_WARNING = "f32497e4bd8c80ef6228f10bd3363f52";
    /** Hash code of first warning. */
    private static final String FIRST_WARNING = "4d839755cabf60eacc6438ac77ac5104";
    /** File in native format. */
    private static final String FINDBUGS_NATIVE_XML = "findbugs-native.xml";
    /** Number of warnings contained in files. */
    private static final int NUMBER_OF_WARNINGS = 2;
    /** Error message. */
    private static final String WRONG_NUMBER_OF_WARNINGS_PARSED = "Wrong number of bugs parsed.";

    private MavenModule parseFile(final String fileName, final boolean isRankActivated) throws IOException, SAXException, DocumentException {
        return parseFile(fileName, isRankActivated, null, null);
    }

    private MavenModule parseFile(final String fileName, final boolean isRankActivated, final String excludePattern, final String includePattern) throws IOException, SAXException, DocumentException {
        Collection<FileAnnotation> annotations = new FindBugsParser(isRankActivated, excludePattern, includePattern).parse(new FindBugsParser.InputStreamProvider() {
            public InputStream getInputStream() throws IOException {
                return FindBugsParserTest.class.getResourceAsStream(fileName);
            }
        }, new ArrayList<String>(), fileName);
        MavenModule module = new MavenModule(fileName);
        if (!annotations.isEmpty()) {
            module.setName(annotations.iterator().next().getModuleName());
        }
        module.addAnnotations(annotations);

        return module;
    }

    /**
     * Parses fb-contrib messages.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void issue7238() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile("issue7238.xml", false);
        assertEquals("Wrong number of warnings", 1820, module.getNumberOfAnnotations());
    }



    /**
     * Parses fb-contrib messages.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void issue7238withIncludePattern() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile("issue7238.xml", false, null, "*gti/plc/test*,*gti/plc/server/siemens/libnodave*,*gti/plc/util*");
        assertEquals("Wrong number of warnings", 68, module.getNumberOfAnnotations());
    }

    /**
     * Parses fb-contrib messages.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void issue7238withExcludePattern() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile("issue7238.xml", false, "*gti/plc/test*,*gti/plc/server/siemens/libnodave*,*gti/plc/util*", null);
        assertEquals("Wrong number of warnings", 1752, module.getNumberOfAnnotations());
    }

    /**
     * Parses fb-contrib messages.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void issue7238withIncludeExcludePattern() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile("issue7238.xml", false,"*gti/plc/server/siemens/libnodave*","*gti/plc/test*,*gti/plc/server/siemens/libnodave*,*gti/plc/util*");
        assertEquals("Wrong number of warnings", 57, module.getNumberOfAnnotations());
    }

    /**
     * Parses fb-contrib messages.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-12314">Issue 12314</a>
     */
    @Test
    public void issue12314() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile("issue12314.xml", false);
        assertEquals("Wrong number of warnings", 1, module.getNumberOfAnnotations());

        checkAnnotation(module.getAnnotations().iterator().next(),
                "issue12314.xml", Priority.NORMAL,
                "com/sedsystems/core/valid/Transformers.java", "com.sedsystems.core.valid",
                60, 60, 1);
    }

    /**
     * Checks that the SAX property is overwritten with Xerces if it has been set to another value.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7312">Issue 7312</a>
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7932">Issue 7932</a>
     */
    @Test
    public void issue7312and7932() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        String saxParser = this.getClass().getName();
        System.setProperty(FindBugsParser.SAX_DRIVER_PROPERTY, saxParser);
        MavenModule module = parseFile("issue7312.xml", false);
        assertEquals("Wrong number of warnings", 0, module.getNumberOfAnnotations());
        assertEquals("Wrong sax parser property", saxParser, System.getProperty(FindBugsParser.SAX_DRIVER_PROPERTY));
    }

    /**
     * Checks that the SAX property is not touched if it is null.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     * @see <a href="http://issues.jenkins-ci.org/browse/JENKINS-7932">Issue 7932</a>
     */
    @Test
    public void issue7932OOnNull() throws IOException, SAXException, DocumentException {
        FindBugsMessages.getInstance().initialize();

        System.clearProperty(FindBugsParser.SAX_DRIVER_PROPERTY);
        MavenModule module = parseFile("issue7312.xml", false);
        assertEquals("Wrong number of warnings", 0, module.getNumberOfAnnotations());
        assertNull("Wrong sax parser property", System.getProperty(FindBugsParser.SAX_DRIVER_PROPERTY));
    }

    /**
     * Tests the message mapping.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     */
    @Test
    public void testMessageMapping() throws SAXException, IOException {
        InputStream stream = FindBugsParserTest.class.getResourceAsStream(FINDBUGS_NATIVE_XML);
        try {
            Map<String, String> mapping = new HashMap<String, String>();
            for (XmlBugInstance bug : new FindBugsParser(false).preparse(stream)) {
                mapping.put(bug.getInstanceHash(), bug.getMessage());
            }
            assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 2, mapping.size());
            assertTrue(BUG_WITH_GIVEN_HASHCODE_NOT_FOUND, mapping.containsKey(FIRST_WARNING));
            assertTrue(BUG_WITH_GIVEN_HASHCODE_NOT_FOUND, mapping.containsKey(SECOND_WARNING));
            assertEquals(
                    WRONG_MESSAGE_PARSED,
                    "Inconsistent synchronization of org.apache.hadoop.dfs.BlockCrcUpgradeObjectDatanode.blocksPreviouslyUpgraded; locked 85% of time",
                    mapping.get(FIRST_WARNING));
            assertEquals(
                    WRONG_MESSAGE_PARSED,
                    "Should org.apache.hadoop.streaming.StreamJob$MultiPropertyOption be a _static_ inner class?",
                    mapping.get(SECOND_WARNING));
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void testFileWithMultipleLinesAndRanges() throws IOException, SAXException, DocumentException {
        scanNativeFile(FINDBUGS_NATIVE_XML, FINDBUGS_NATIVE_XML,
                Priority.NORMAL, "org/apache/hadoop/dfs/BlockCrcUpgrade.java", "org.apache.hadoop.dfs", 1309, 1309,
                5, "org/apache/hadoop/streaming/StreamJob.java", "org.apache.hadoop.streaming", 935, 980, 1, false);
        scanNativeFile(FINDBUGS_NATIVE_XML, FINDBUGS_NATIVE_XML,
                Priority.LOW, "org/apache/hadoop/dfs/BlockCrcUpgrade.java", "org.apache.hadoop.dfs", 1309, 1309,
                5, "org/apache/hadoop/streaming/StreamJob.java", "org.apache.hadoop.streaming", 935, 980, 1, true);
    }

    /**
     * Checks whether, if a bug instance contains more than one element, we
     * correctly take the first one as referring to the buggy class.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void scanFileWarningsHaveMultipleClasses() throws IOException, SAXException, DocumentException {
        scanNativeFile("findbugs-multclass.xml", "FindBugs",
                Priority.HIGH, "umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 82, 82,
                1, "edu/umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 93, 93, 1, false);
        scanNativeFile("findbugs-multclass.xml", "FindBugs",
                Priority.LOW, "umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 82, 82,
                1, "edu/umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 93, 93, 1, true);
    }

    /**
     * Checks whether we could also parse bugs of the fbcontrib plug-in.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void scanFbContribFile() throws IOException, SAXException, DocumentException {
        MavenModule parseFile = parseFile("fbcontrib.xml", false);
        JavaPackage javaPackage = parseFile.getPackage("hudson.plugins.tasks");
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 16, javaPackage.getNumberOfAnnotations());

        boolean found = false;
        for (FileAnnotation annotation : javaPackage.getAnnotations()) {
            assertFalse("Message is not resolved.", annotation.getMessage().contains("TEST:"));
            if (annotation.getFileName().contains("ResultSummary.java")) {
                found = true;
                assertFalse("Warning message could not be resolved.",
                        annotation.getToolTip().contains(
                                    "A warning was recorded, but findbugs can't find the description of this bug pattern"));
            }
        }
        assertTrue("No warning in class ResultSummary.java found.", found);
    }

    /**
     * Checks whether we generate a message if there is no message in the XML file.
     *
     * @throws IOException
     *             in case of an error
     * @throws SAXException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    @Test
    public void handleFilesWithoutMessages() throws IOException, SAXException, DocumentException {
        MavenModule module = parseFile("findbugs-nomessage.xml", false);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 1, module.getNumberOfAnnotations());

        FileAnnotation next = module.getAnnotations().iterator().next();
        assertTrue("Warning has no message.", next.getMessage().contains("Redundant nullcheck of"));
        assertEquals("Wrong category", "STYLE", next.getCategory());
        assertEquals("Wrong category", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", next.getType());
    }

    /**
     * Tests that a file with 2 warnings (1 not a bug consensus) is handled
     * correctly, i.e., only one warning is returned.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DocumentException
     *             the document exception
     * @throws SAXException
     *             in case of an error
     */
    @Test
    public void handleFileWithNotABugConsensus() throws IOException, SAXException, DocumentException {
        MavenModule module = parseFile("findbugs-with-notAProblem-bug.xml", false);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 1, module.getNumberOfAnnotations());

        FileAnnotation next = module.getAnnotations().iterator().next();
        assertTrue("Warning has no message.", next.getMessage().contains("Redundant nullcheck of"));
        assertEquals("Wrong category", "STYLE", next.getCategory());
        assertEquals("Wrong category", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", next.getType());
    }

    /**
     * Shows that a file with first seen date is handled correctly.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DocumentException
     *             the document exception
     * @throws SAXException
     *             in case of an error
     */
    @Test
    public void handleFileWithFirstSeenDate() throws IOException, SAXException, DocumentException {
        MavenModule module = parseFile("findbugs-with-firstSeen.xml", false);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 1, module.getNumberOfAnnotations());

        FileAnnotation next = module.getAnnotations().iterator().next();
        assertTrue("Warning has no message.", next.getMessage().contains("Redundant nullcheck of"));
        assertEquals("Wrong category", "STYLE", next.getCategory());
        assertEquals("Wrong category", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", next.getType());
        assertTrue("Should contain cloud info: " + next.getMessage(),
                   next.getMessage().matches(".*First seen .* at 4/11/10 11:24 AM.*"));
    }

    /**
     * Shows that a file with reviews is handled correctly.
     *
     * @throws SAXException
     *             in case of an error
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DocumentException
     *             the document exception
     */
    @Test
    public void handleFileWithReviews() throws IOException, SAXException, DocumentException {
        MavenModule module = parseFile("findbugs-with-reviews.xml", false);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 1, module.getNumberOfAnnotations());

        FileAnnotation next = module.getAnnotations().iterator().next();
        assertTrue("Warning has no message.", next.getMessage().contains("Redundant nullcheck of"));
        assertEquals("Wrong category", "STYLE", next.getCategory());
        assertEquals("Wrong category", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", next.getType());
        assertTrue("Should contain cloud info: " + next.getMessage(),
                   next.getMessage().matches(".*4 comments.*"));
    }

    /**
     * Verifies that third party categories are correctly parsed.
     *
     * @throws SAXException
     *             in case of an error
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws DocumentException
     *             the document exception
     */
    @Test
    public void thirdPartyCategory() throws IOException, SAXException, DocumentException {
        MavenModule module = parseFile("findbugs-3rd-party-category.xml", false);
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, 2, module.getNumberOfAnnotations());
        Iterator<FileAnnotation> annotations = module.getAnnotations().iterator();
        FileAnnotation next = annotations.next();
        assertEquals("Wrong serial version ID: ", "SE_NO_SERIALVERSIONID", next.getType());
        assertEquals("Wrong category: ", "BAD_PRACTICE", next.getCategory());
        next = annotations.next();
        assertEquals("Wrong type: ", "WEAK_MESSAGE_DIGEST", next.getType());
        assertEquals("Wrong category: ", "SECURITY", next.getCategory());
    }

    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    private void scanNativeFile(final String findbugsFile, final String projectName,
            final Priority priority, final String fileName1, final String packageName1, final int start1, final int end1,
            final int ranges1, final String fileName2, final String packageName2, final int start2, final int end2, final int ranges2, final boolean isRankActivated)
            throws IOException, SAXException, DocumentException {
   // CHECKSTYLE:ON
        FindBugsMessages.getInstance().initialize();

        MavenModule module = parseFile(findbugsFile, isRankActivated);
        assertEquals("Wrong project name guessed", projectName, module.getName());

        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, NUMBER_OF_WARNINGS, module.getNumberOfAnnotations());
        Collection<FileAnnotation> warnings = module.getAnnotations();
        assertEquals(WRONG_NUMBER_OF_WARNINGS_PARSED, NUMBER_OF_WARNINGS, warnings.size());


        Iterator<FileAnnotation> annotations = warnings.iterator();
        FileAnnotation annotation1 = annotations.next();
        FileAnnotation annotation2 = annotations.next();

        FileAnnotation firstAnnotation;
        FileAnnotation secondAnnotation;
        if (fileName1.equals(annotation1.getFileName())) {
            firstAnnotation = annotation1;
            secondAnnotation = annotation2;
        }
        else {
            firstAnnotation = annotation2;
            secondAnnotation = annotation1;
        }

        checkAnnotation(firstAnnotation, projectName, priority, fileName1, packageName1, start1, end1, ranges1);
        checkAnnotation(secondAnnotation, projectName, priority, fileName2, packageName2, start2, end2, ranges2);
    }

    /**
     * Checks an individual annotation.
     *
     * @param annotation
     *            the annotation to check
     * @param projectName
     *            name of the project
     * @param priority
     *            priority
     * @param fileName
     *            filename
     * @param packageName
     *            package name
     * @param start
     *            start line
     * @param end
     *            end line
     * @param ranges
     *            number of line ranges for first class
     */
    // CHECKSTYLE:OFF
    private void checkAnnotation(final FileAnnotation annotation, final String projectName, final Priority priority, final String fileName,
            final String packageName, final int start, final int end, final int ranges) {
    // CHECKSTYLE:ON
        assertEquals("Wrong file name parsed.", fileName, annotation.getFileName());
        assertEquals("Wrong package name parsed.", packageName, annotation.getPackageName());
        assertEquals("Wrong module name parsed.", projectName, annotation.getModuleName());

        assertFalse("Warning message could not be resolved.", annotation.getToolTip().contains("A warning was recorded, but findbugs can't find the description of this bug pattern"));

        Collection<LineRange> lineRanges = annotation.getLineRanges();
        assertEquals("Wrong number of line ranges parsed.", ranges, lineRanges.size());

        LineRange range = lineRanges.iterator().next();
        assertEquals("Wrong start of line range.", start, range.getStart());
        assertEquals("Wrong end of line range.", end, range.getEnd());

        assertEquals("Wrong priority parsed.", priority, annotation.getPriority());
        assertEquals("Wrong start of line range", start, annotation.getPrimaryLineNumber());

        assertFalse("No message for bug pattern detected", annotation.getToolTip().equals(Messages.FindBugs_Publisher_NoMessageFoundText()));
    }
}
