package hudson.plugins.findbugs.util;

import static org.junit.Assert.*;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 *  Tests the class {@link MavenModuleDetector}.
 */
public class MavenModuleDetectorTest {
    /**
     * Checks whether we could identify a maven module that is at the top level of the filename.
     */
    @Test
    public void testTopLevelModuleName() {
        String moduleName = MavenModuleDetector.guessModuleName("com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals("Wrong module name guessed.", "com.avaloq.adt.core", moduleName);
    }

    /**
     * Checks whether we could identify a java package name and maven module.
     */
    @Test
    public void testSubModuleName() {
        String moduleName = MavenModuleDetector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/src/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals("Wrong module name guessed.", "com.avaloq.adt.core", moduleName);
    }

    /**
     * Checks whether we return an empty string if we can't guess the module name.
     */
    @Test
    public void testEmptyString() {
        String moduleName = MavenModuleDetector.guessModuleName("base/com.hello.world/com.avaloq.adt.core/source/com/avaloq/adt/core/job/AvaloqJob.java");
        assertEquals("Wrong module name guessed.", StringUtils.EMPTY, moduleName);
    }
}
