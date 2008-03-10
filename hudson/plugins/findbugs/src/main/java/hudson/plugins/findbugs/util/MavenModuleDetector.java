package hudson.plugins.findbugs.util;

import org.apache.commons.lang.StringUtils;

/**
 * Detects maven module names by parsing the name of a source file.
 */
public final class MavenModuleDetector {
    /**
     * Guesses a maven module name based on the source folder.
     *
     * @param fileName
     *            the absolute path of the file (UNIX style) to guess the module
     *            for
     * @return the guessed module name or an empty string
     */
    public static String guessModuleName(final String fileName) {
        if (fileName.contains("/src")) {
            String module = StringUtils.substringBefore(fileName, "/src/");
            if (module.contains("/")) {
                module = StringUtils.substringAfterLast(module, "/");
            }
            if (StringUtils.isNotBlank(module)) {
                return module;
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Creates a new instance of <code>MavenModuleDetector</code>.
     */
    private MavenModuleDetector() {
        // prevents instantiation
    }
}

