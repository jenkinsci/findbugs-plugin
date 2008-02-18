package hudson.plugins.findbugs.parser.maven;

import hudson.FilePath;
import hudson.plugins.findbugs.model.MavenModule;
import hudson.plugins.findbugs.model.Priority;
import hudson.plugins.findbugs.model.WorkspaceFile;
import hudson.plugins.findbugs.parser.Bug;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * A parser for the maven-findbugs-plugin XML files.
 */
public class MavenFindBugsParser {
    /**
     * Returns whether this parser accepts the specified file format.
     *
     * @param file
     *            the file to parse
     * @return <code>true</code> if the provided file is in maven format.
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    public boolean accepts(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(MavenFindBugsParser.class.getClassLoader());

        digester.addObjectCreate("BugCollection/file/BugInstance", BugCollection.class);

        BugCollection module = (BugCollection)digester.parse(file);

        return module != null;
    }

    /**
     * Returns whether the specified FindBugs file defines source paths.
     *
     * @param file
     *            the file to check
     * @return <code>true</code> if the specified FindBugs file defines source paths
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    public boolean hasSourcePaths(final InputStream file) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(MavenFindBugsParser.class.getClassLoader());

        digester.addObjectCreate("BugCollection/Project/SrcDir", BugCollection.class);

        BugCollection module = (BugCollection)digester.parse(file);

        return module != null;
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the Maven FindBugs plug-in format.
     *
     * @param file
     *            the FindBugs analysis file
     * @param moduleName
     *            name of the maven module
     * @param workspace workspace root
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     * @throws InterruptedException
     *             if the user aborts the mapping
     */
    public MavenModule parse(final InputStream file, final String moduleName, final File workspace) throws IOException, SAXException, InterruptedException {
        MavenModule mavenModule = parse(file, moduleName);
        mapClassesToFiles(workspace, mavenModule);

        return mavenModule;
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the Maven FindBugs plug-in format.
     *
     * @param file
     *            the FindBugs analysis file
     * @param moduleName
     *            name of the maven module
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws SAXException
     *             if the file is not in valid XML format
     */
    public MavenModule parse(final InputStream file, final String moduleName) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(MavenFindBugsParser.class.getClassLoader());

        String rootXPath = "BugCollection";
        digester.addObjectCreate(rootXPath, BugCollection.class);
        digester.addSetProperties(rootXPath);

        String fileXPath = "BugCollection/file";
        digester.addObjectCreate(fileXPath, hudson.plugins.findbugs.parser.maven.File.class);
        digester.addSetProperties(fileXPath);
        digester.addSetNext(fileXPath, "addFile", hudson.plugins.findbugs.parser.maven.File.class.getName());

        String bugXPath = "BugCollection/file/BugInstance";
        digester.addObjectCreate(bugXPath, BugInstance.class);
        digester.addSetProperties(bugXPath, "lineNumber", "lineNumberExpression");
        digester.addSetNext(bugXPath, "addBugInstance", BugInstance.class.getName());

        BugCollection module = (BugCollection)digester.parse(file);
        if (module == null) {
            throw new IllegalArgumentException("Input stream is not in maven-findbugs-plugin format.");
        }

        return convert(module, moduleName);
    }

    /**
     * FIXME: Document method mapClassesToFiles
     * @param workspace
     * @param mavenModule
     * @throws IOException
     * @throws InterruptedException
     */
    private void mapClassesToFiles(final File workspace, final MavenModule mavenModule)
            throws IOException, InterruptedException {
        new FilePath(workspace).act(new WorkspaceScanner(mavenModule));
    }

    /**
     * Converts the internal structure to the annotations API.
     *
     * @param collection
     *            the internal maven module
     * @param moduleName
     *            name of the maven module
     * @return a maven module of the annotations API
     */
    private MavenModule convert(final BugCollection collection, final String moduleName) {
        MavenModule module = new MavenModule(moduleName);

        for (hudson.plugins.findbugs.parser.maven.File file : collection.getFiles()) {
            WorkspaceFile workspaceFile = new WorkspaceFile();
            for (BugInstance warning : file.getBugInstances()) {
                Priority priority = Priority.valueOf(StringUtils.upperCase(warning.getPriority()));
                Bug bug;
                if (warning.isLineAnnotation()) {
                    bug = new Bug(priority, warning.getMessage(), warning.getCategory(), warning.getType(), warning.getLineNumber());
                }
                else {
                    bug = new Bug(priority, warning.getMessage(), warning.getCategory(), warning.getType());
                }
                workspaceFile.addAnnotation(bug);
            }
            workspaceFile.setPackageName(StringUtils.substringBeforeLast(file.getClassname(), "."));
            workspaceFile.setModuleName(moduleName);
            workspaceFile.setName(StringUtils.substringAfterLast(file.getClassname(), "."));
            module.addAnnotations(workspaceFile.getAnnotations());
        }
        return module;
    }
}

