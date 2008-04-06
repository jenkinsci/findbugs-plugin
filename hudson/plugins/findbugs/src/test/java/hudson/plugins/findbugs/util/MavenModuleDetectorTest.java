package hudson.plugins.findbugs.util;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 *  Tests the class {@link MavenModuleDetector}.
 */
public class MavenModuleDetectorTest {
    /** Expected module name for all tests. */
    private static final String EXPECTED_MODULE = "com.avaloq.adt.core";
    /** JUnit Error message. */
    private static final String ERROR_MESSAGE = "Wrong module name detected.";

    /**
     * Checks whether we could identify a maven module that is at the top level of the filename.
     */
    @Test
    public void testTopLevelModuleName() {
        String moduleName = MavenModuleDetector.guessModuleName("com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
        moduleName = MavenModuleDetector.guessModuleName("com.avaloq.adt.core\\src\\com\\avaloq\\adt\\core\\job\\AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
    }

    /**
     * Checks whether we could identify a java package name and maven module.
     */
    @Test
    public void testSubModuleName() {
        String moduleName = MavenModuleDetector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);
    }

    /**
     * Checks whether we could identify a java package name and maven module.
     */
    @Test
    public void testTargetName() {
        String moduleName = MavenModuleDetector.guessModuleName("X:\\Build\\Results\\jobs\\ADT-Base\\workspace\\com.avaloq.adt.core\\target\\pmd.xml");
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, moduleName);

        String input = "workspace/com.avaloq.adt.core/target/findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, MavenModuleDetector.guessModuleName(input));

        input = "com.avaloq.adt.core/target/findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, MavenModuleDetector.guessModuleName(input));

        input = "X:\\work\\workspace\\com.avaloq.adt.core\\target\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, MavenModuleDetector.guessModuleName(input));

        input = "com.avaloq.adt.core\\target\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, EXPECTED_MODULE, MavenModuleDetector.guessModuleName(input));

        input = "com.avaloq.adt.core\\findbugs.xml";
        assertEquals(ERROR_MESSAGE, "", MavenModuleDetector.guessModuleName(input));
    }

    /**
     * Checks whether we could identify a java package name and maven module.
     *
     * @throws URISyntaxException
     *             should never happen
     */
    @Test
    public void testPomName() throws URISyntaxException {
        String uri = MavenModuleDetectorTest.class.getResource("pom.xml").toURI().getPath();
        String target = uri.replace("pom.xml", "target");
        assertEquals(ERROR_MESSAGE, "ADT Business Logic", MavenModuleDetector.guessModuleName(target.substring(1)));
    }

    /**
     * Checks whether we return an empty string if we can't guess the module name.
     */
    @Test
    public void testEmptyString() {
        String moduleName = MavenModuleDetector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/source/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals(ERROR_MESSAGE, StringUtils.EMPTY, moduleName);
    }


}
