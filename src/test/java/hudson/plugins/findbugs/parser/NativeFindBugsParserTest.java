package hudson.plugins.findbugs.parser;

import static org.junit.Assert.*;
import hudson.plugins.findbugs.model.FileAnnotation;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.parser.NativeFindBugsParser;

import java.io.IOException;
import java.util.Collection;

import org.dom4j.DocumentException;
import org.junit.Test;

/**
 *  Tests the extraction of FindBugs analysis results.
 */
public class NativeFindBugsParserTest {
    /** Error message. */
    private static final String NO_FILE_NAME_FOUND = "No file name found.";
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
    public void scanNativeFile() throws IOException, DocumentException {
        MavenModule module = parseFile("findbugs-native.xml");

        assertEquals(ERROR_MESSAGE, 136, module.getNumberOfAnnotations());

        // FIXME: check this
//        for (FileAnnotation warning : module.getPackage("org.apache.hadoop.ipc").getAnnotations()) {
//            assertNotNull("Message should not be empty.", warning.getMessage());
//            assertNotNull("Line number should not be empty.", warning.getLineNumber());
//
//            assertNotNull(NO_FILE_NAME_FOUND, warning.getWorkspaceFile().getName());
//        }
    }

    /**
     * Checks whether, if a bug instance contains more than one
     * element, we correctly take the first one as referring to the
     * buggy class.
     */
    @Test
    public void scanFileWarningsHaveMultipleClasses() throws IOException, DocumentException {
        MavenModule module = parseFile("findbugs-multclass.xml");

        assertEquals(ERROR_MESSAGE, 2, module.getNumberOfAnnotations());
        Collection<FileAnnotation> warnings = module.getAnnotations();
        for (FileAnnotation warning : warnings) {
            assertTrue("Wrong package prefix found.", warning.getWorkspaceFile().getPackageName().startsWith("edu.umd"));
            assertNotNull(NO_FILE_NAME_FOUND, warning.getWorkspaceFile().getName());
        }
    }

    /**
     * Checks whether we correctly assign source paths when the source directory
     * folder is specified in the FindBugs native file format. element, we
     * correctly take the first one as referring to the buggy class.
     */
    @Test
    public void checkSourcePathComposition() throws IOException, DocumentException {
        MavenModule module = parseFile("srcpath.xml");

        assertEquals(ERROR_MESSAGE, 1, module.getNumberOfAnnotations());
        FileAnnotation warning = module.getAnnotations().iterator().next();

        // FIXME: check this
//        assertEquals("Wrong filename guessed.", "/usr/local/tomcat/hudson/jobs/FindBugs Test/workspace/findBugsTest/src/org/example/SyncBug.java", warning.getWorkspaceFile().getName());
    }
}