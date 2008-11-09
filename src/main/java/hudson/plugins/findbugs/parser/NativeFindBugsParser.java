package hudson.plugins.findbugs.parser;

import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.util.model.FileAnnotation;
import hudson.plugins.findbugs.util.model.LineRange;
import hudson.plugins.findbugs.util.model.Priority;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.ba.SourceFile;
import edu.umd.cs.findbugs.ba.SourceFinder;

/**
 * A parser for the native FindBugs XML files (ant task, batch file or
 * maven-findbugs-plugin >= 1.2).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class NativeFindBugsParser {
    static {
        DetectorFactoryCollection.rawInstance().setPluginList(new URL[0]);
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the native FindBugs format.
     * @param file
     *            the FindBugs analysis file
     * @param sources
     *            a collection of folders to scan for source files
     * @param moduleName
     *            name of maven module
     *
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException
     * @throws SAXException
     */
    public Collection<FileAnnotation> parse(final File file, final Collection<String> sources, final String moduleName)
            throws IOException, DocumentException, SAXException {

        Map<String, String> hashToMessageMapping = createHashToMessageMapping(new FileInputStream(file));

        return parse(new FileInputStream(file), sources, moduleName, hashToMessageMapping);
    }

    /**
     * Creates a mapping of FindBugs warnings to messages. A bug is represented
     * by its unique hash code.
     *
     * @param file
     *            the FindBugs XML file
     * @return the map of warning messages
     * @throws SAXException
     *             if the file contains no valid XML
     * @throws IOException
     *             signals that an I/O exception has occurred.
     */
    public Map<String, String> createHashToMessageMapping(final InputStream file) throws SAXException, IOException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.setClassLoader(NativeFindBugsParser.class.getClassLoader());

        String rootXPath = "BugCollection/BugInstance";
        digester.addObjectCreate(rootXPath, XmlBugInstance.class);
        digester.addSetProperties(rootXPath);

        String fileXPath = rootXPath + "/LongMessage";
        digester.addCallMethod(fileXPath, "setMessage", 0);

        digester.addSetNext(rootXPath, "add", Object.class.getName());
        ArrayList<XmlBugInstance> bugs = new ArrayList<XmlBugInstance>();
        digester.push(bugs);
        digester.parse(file);

        HashMap<String, String> mapping = new HashMap<String, String>();
        for (XmlBugInstance bug : bugs) {
            mapping.put(bug.getInstanceHash(), bug.getMessage());
        }
        return mapping;
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in
     * the native FindBugs format.
     *
     * @param file
     *            the FindBugs analysis file
     * @param sources
     *            a collection of folders to scan for source files
     * @param moduleName
     *            name of maven module
     * @param hashToMessageMapping
     *            mapping of hash codes to messages
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException in case of a parser exception
     */
    public Collection<FileAnnotation> parse(final InputStream file, final Collection<String> sources,
            final String moduleName, final Map<String, String> hashToMessageMapping) throws IOException,
            DocumentException {
        Project project = createMavenProject(sources);

        SortedBugCollection collection = new SortedBugCollection();
        collection.readXML(file, project);

        SourceFinder sourceFinder = new SourceFinder();
        sourceFinder.setSourceBaseList(project.getSourceDirList());

        String actualName = extractModuleName(moduleName, project);

        ArrayList<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
        Collection<BugInstance> bugs = collection.getCollection();
        for (BugInstance warning : bugs) {
            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();

            String message = warning.getMessage();
            if (message.contains("TEST: Unknown warning")) {
                message = FindBugsMessages.getInstance().getShortMessage(warning.getType());
            }
            Bug bug = new Bug(getPriority(warning),
                    StringUtils.defaultIfEmpty(hashToMessageMapping.get(warning.getInstanceHash()), message), warning.getBugPattern().getCategory(),
                        warning.getType(), sourceLine.getStartLine(), sourceLine.getEndLine());
            bug.setInstanceHash(warning.getInstanceHash());

            Iterator<BugAnnotation> annotationIterator = warning.annotationIterator();
            while (annotationIterator.hasNext()) {
                BugAnnotation bugAnnotation = annotationIterator.next();
                if (bugAnnotation instanceof SourceLineAnnotation) {
                    SourceLineAnnotation annotation = (SourceLineAnnotation)bugAnnotation;
                    bug.addLineRange(new LineRange(annotation.getStartLine(), annotation.getEndLine()));
                }
            }
            String fileName;
            try {
                SourceFile sourceFile = sourceFinder.findSourceFile(sourceLine);
                fileName = sourceFile.getFullFileName();
            }
            catch (IOException exception) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Can't resolve absolute file name for file " + sourceLine.getSourceFile()
                        + ", dir list = " + project.getSourceDirList().toString());
                fileName = sourceLine.getPackageName().replace(".", "/") + "/" + sourceLine.getSourceFile();
            }
            bug.setFileName(fileName);
            bug.setPackageName(warning.getPrimaryClass().getPackageName());
            bug.setModuleName(actualName);

            annotations.add(bug);
        }
        return annotations;
    }

    /**
     * Maps the FindBugs priority to our priority enumeration.
     *
     * @param warning
     *            the FindBugs warning
     * @return mapped priority enumeration
     */
    private Priority getPriority(final BugInstance warning) {
        switch (warning.getPriority()) {
            case 1:
                return Priority.HIGH;
            case 2:
                return Priority.NORMAL;
            default:
                return Priority.LOW;
        }
    }

    /**
     * Extracts the module name from the specified project. If empty then the
     * provided default name is used.
     *
     * @param defaultName
     *            the default module name to use
     * @param project
     *            the maven 2 project
     * @return the module name to use
     */
    private String extractModuleName(final String defaultName, final Project project) {
        if (StringUtils.isBlank(project.getProjectName())) {
            return defaultName;
        }
        else {
            return project.getProjectName();
        }
    }

    /**
     * Creates a maven project with some predefined source paths.
     *
     * @param sources
     *            a collection of folders to scan for source files
     * @return the new project
     */
    private Project createMavenProject(final Collection<String> sources) {
        Project project = new Project();
        for (String sourceFolder : sources) {
            project.addSourceDir(sourceFolder);
        }
        return project;
    }
}

