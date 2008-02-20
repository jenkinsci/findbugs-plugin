package hudson.plugins.findbugs.parser;

import static org.junit.Assert.*;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.LineRange;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.Priority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.dom4j.DocumentException;
import org.junit.Test;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class NativeFindBugsParserTest {
    /** Number of warnings contained in files. */
    private static final int NUMBER_OF_WARNINGS = 2;
    /** Error message. */
    private static final String ERROR_MESSAGE = "Wrong number of bugs parsed.";

    /**
     * Parses the specified file.
     *
     * @param fileName the file to read
     * @return the parsed module
     * @throws IOException
     *             in case of an error
     * @throws DocumentException
     *             in case of an error
     */
    private MavenModule parseFile(final String fileName) throws IOException, DocumentException {
        return new NativeFindBugsParser().parse(NativeFindBugsParserTest.class.getResourceAsStream(fileName), "", fileName);
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     * @throws DocumentException
     */
    @Test
    public void scanFileWithMultipleLinesAndRanges() throws IOException, DocumentException {
        scanNativeFile("findbugs-native.xml", "TestCase",
                Priority.NORMAL, "org/apache/hadoop/dfs/BlockCrcUpgrade.java", "org.apache.hadoop.dfs", 1309, 1309,
                5, "org/apache/hadoop/streaming/StreamJob.java", "org.apache.hadoop.streaming", 935, 980, 1);
    }

    /**
     * Checks whether, if a bug instance contains more than one
     * element, we correctly take the first one as referring to the
     * buggy class.
     */
    @Test
    public void scanFileWarningsHaveMultipleClasses() throws IOException, DocumentException {
        scanNativeFile("findbugs-multclass.xml", "FindBugs",
                Priority.HIGH, "umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 82, 82,
                1, "edu/umd/cs/findbugs/PluginLoader.java", "edu.umd.cs.findbugs", 93, 93, 1);
    }

    /**
     * Checks whether we correctly detect a file in FindBugs native format.
     *
     * @param findbugsFile
     *            name of the file to read
     * @param projectName
     *            name of the project
     * @param priority
     *            priority
     * @param fileName1
     *            first class filename
     * @param packageName1
     *            first class package name
     * @param start1
     *            start line of first class
     * @param end1
     *            end line of first class
     * @param ranges1
     *            number of line ranges for first class
     * @param fileName2
     *            second class filename
     * @param packageName2
     *            second class package name
     * @param start2
     *            start line of second class
     * @param end2
     *            end line of second class
     * @param ranges2
     *            number of line ranges for second class
     * @throws DocumentException
     *             on a parse error
     */
    // CHECKSTYLE:OFF
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public void scanNativeFile(final String findbugsFile, final String projectName,
            final Priority priority, final String fileName1, final String packageName1, final int start1, final int end1,
            final int ranges1, final String fileName2, final String packageName2, final int start2, final int end2, final int ranges2) throws IOException, DocumentException {
   // CHECKSTYLE:ON
        MavenModule module = parseFile(findbugsFile);
        assertEquals(projectName, module.getName());

        assertEquals(ERROR_MESSAGE, NUMBER_OF_WARNINGS, module.getNumberOfAnnotations());
        Collection<FileAnnotation> warnings = module.getAnnotations();
        assertEquals(ERROR_MESSAGE, NUMBER_OF_WARNINGS, warnings.size());

        Iterator<FileAnnotation> annotations = warnings.iterator();
        FileAnnotation annotation1 = annotations.next();
        FileAnnotation annotation2 = annotations.next();

        FileAnnotation firstAnnotation;
        FileAnnotation secondAnnotation;
        if (fileName1.equals(annotation1.getWorkspaceFileName())) {
            firstAnnotation = annotation1;
            secondAnnotation = annotation2;
        }
        else {
            firstAnnotation = annotation2;
            secondAnnotation = annotation1;
        }

        checkAnnotation(firstAnnotation, priority, fileName1, packageName1, start1, end1, ranges1);
        checkAnnotation(secondAnnotation, priority, fileName2, packageName2, start2, end2, ranges2);
    }

    /**
     * Checks an individual annotation.
     *
     * @param annotation
     *            the annotation to check
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
    private void checkAnnotation(final FileAnnotation annotation, final Priority priority, final String fileName,
            final String packageName, final int start, final int end, final int ranges) {
        assertEquals("Wrong file name parsed.", fileName, annotation.getWorkspaceFileName());
        assertEquals("Wrong package name parsed.", packageName, annotation.getWorkspaceFile().getPackageName());

        Collection<LineRange> lineRanges = annotation.getLineRanges();
        assertEquals("Wrong number of line ranges parsed.", ranges, lineRanges.size());

        LineRange range = lineRanges.iterator().next();
        assertEquals("Wrong start of line range.", start, range.getStart());
        assertEquals("Wrong end of line range.", end, range.getEnd());

        assertEquals("Wrong priority parsed.", priority, annotation.getPriority());
        assertEquals("Wrong start of line range", start, annotation.getPrimaryLineNumber());
    }
}