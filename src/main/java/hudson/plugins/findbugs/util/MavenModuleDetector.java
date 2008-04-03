package hudson.plugins.findbugs.util;

import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

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
     * @return the guessed module name or an empty string if the name could not be
     *         resolved
     */
    public static String guessModuleName(final String fileName) {
        String unixName = fileName.replace("\\", "/");
        String module = checkPrefix(unixName, "/src");
        if (StringUtils.isNotBlank(module)) {
            return module;
        }
        module = checkPrefix(unixName, "/target");
        if (StringUtils.isNotBlank(module)) {
            return module;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Checks whether the filename contains the specified folder. The module
     * name is the path segment before this folder.
     *
     * @param file
     *            the file name in UNIX format
     * @param folder
     *            the folder that directly follows the module name
     * @return the module name or an empty string if the name could not be
     *         resolved
     */
    private static String checkPrefix(final String file, final String folder) {
        if (file.contains(folder)) {
            String module = StringUtils.substringBeforeLast(file, folder);
            String projectName = parsePom(module);
            if (!StringUtils.isBlank(projectName)) {
                return projectName;
            }
            if (module.contains("/")) {
                module = StringUtils.substringAfterLast(module, "/");
            }
            return module;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Returns the project name stored in the POM.
     *
     * @param fileName
     *            maven module root folder
     * @return the project name or an empty string if the name could not be
     *         resolved
     */
    private static String parsePom(final String fileName) {
        try {
            java.io.File pom = new java.io.File(fileName + "/pom.xml");
            if (pom.exists()) {
                Digester digester = new Digester();
                digester.setValidating(false);
                digester.setClassLoader(MavenModuleDetector.class.getClassLoader());

                digester.push(new StringBuffer());
                digester.addCallMethod("project/name", "append", 0);

                StringBuffer result = (StringBuffer)digester.parse(pom);
                return result.toString();
            }
        }
        catch (IOException exception) {
            // ignore
        }
        catch (SAXException exception) {
            // ignore
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

