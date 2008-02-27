package hudson.plugins.findbugs.parser.maven;

import hudson.FilePath;
import hudson.plugins.findbugs.parser.Bug;
import hudson.plugins.findbugs.util.model.MavenModule;
import hudson.plugins.findbugs.util.model.Priority;
import hudson.plugins.findbugs.util.model.WorkspaceFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

/**
 * A parser for the maven-findbugs-plugin XML files (version <= 1.1.1).
 */
public class MavenFindBugsParser {
    /**
     * Returns whether this parser accepts the specified file format.
     *
     * @param file
     *            the file to parse
     * @return <code>true</code> if the provided file is in maven format.
     */
    public boolean accepts(final InputStream file) {
        try {
            Digester digester = new Digester();
            digester.setValidating(false);
            digester.setClassLoader(MavenFindBugsParser.class.getClassLoader());

            digester.addObjectCreate("BugCollection/file/BugInstance", BugCollection.class);

            BugCollection module = (BugCollection)digester.parse(file);

            return module != null;
        }
        catch (IOException exception) {
            return false;
        }
        catch (SAXException exception) {
            return false;
        }
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

        String[] files = new FilePath(workspace).act(new JavaFileFinder());

        mapFiles(mavenModule, files);

        return mavenModule;
    }

    /**
     * Maps each class with an warning to a workspace file.
     *
     * @param mavenModule
     *            the module containing the warnings
     * @param files
     *            the java files in the workspace
     */
    public void mapFiles(final MavenModule mavenModule, final String[] files) {
        HashMap<String, String> fileMapping = new HashMap<String, String>();

        for (int i = 0; i < files.length; i++) {
            String name = files[i].replace('/', '.').replace('\\', '.');
            if (name.contains(".src.main.java.")) {
                String key = StringUtils.substringAfterLast(name, "src.main.java.");
                fileMapping.put(key, files[i]);
            }
            else if (name.contains(".src.test.java.")) {
                String key = StringUtils.substringAfterLast(name, "src.test.java.");
                fileMapping.put(key, files[i]);
            }
            else if (name.contains(".src.")) {
                String key = StringUtils.substringAfterLast(name, "src.");
                fileMapping.put(key, files[i]);
            }
        }
        for (WorkspaceFile file : mavenModule.getFiles()) {
            String key = StringUtils.substringBeforeLast(file.getPackageName() + "." + file.getName(), "$") + ".java";
            if (fileMapping.containsKey(key)) {
                file.setName(fileMapping.get(key));
             }
        }
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
            workspaceFile.setPackageName(StringUtils.substringBeforeLast(file.getClassname(), "."));
            workspaceFile.setModuleName(moduleName);
            workspaceFile.setName(StringUtils.substringAfterLast(file.getClassname(), "."));
            for (BugInstance warning : file.getBugInstances()) {
                Priority priority = Priority.valueOf(StringUtils.upperCase(warning.getPriority()));
                Bug bug = new Bug(priority, warning.getMessage(), warning.getCategory(), warning.getType(),
                            warning.getStart(), warning.getEnd());
                workspaceFile.addAnnotation(bug);
                module.addAnnotation(bug);
            }
        }
        return module;
    }
}

