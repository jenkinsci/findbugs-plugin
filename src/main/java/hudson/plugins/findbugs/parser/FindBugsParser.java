package hudson.plugins.findbugs.parser; // NOPMD

import hudson.plugins.analysis.core.AnnotationParser;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.LineRange;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.findbugs.FindBugsMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.jvnet.localizer.LocaleProvider;
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
public class FindBugsParser implements AnnotationParser {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 8306319007761954027L;

    private static final String DOT = ".";
    private static final String SLASH = "/";

    private static final int DAY_IN_MSEC = 1000 * 60 * 60 * 24;
    private static final int HIGH_PRIORITY_LOWEST_RANK = 4;
    private static final int NORMAL_PRIORITY_LOWEST_RANK = 9;

    static {
        DetectorFactoryCollection.rawInstance().setPluginList(new URL[0]);
    }

    /** Collection of source folders. */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SE")
    private final List<String> mavenSources = new ArrayList<String>();

    /**
     * Creates a new instance of {@link FindBugsParser}.
     */
    public FindBugsParser() {
        this(new ArrayList<String>());
    }

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param sourceFolders
     *            a collection of folders to scan for source files. If empty,
     *            the source folders are guessed.
     */
    public FindBugsParser(final Collection<String> sourceFolders) {
        mavenSources.addAll(sourceFolders);
    }

    /** {@inheritDoc} */
    public Collection<FileAnnotation> parse(final File file, final String moduleName) throws InvocationTargetException {
        try {
            Collection<String> sources = new ArrayList<String>(mavenSources);
            if (sources.isEmpty()) {
                String moduleRoot = StringUtils.substringBefore(file.getAbsolutePath().replace('\\', '/'), "/target/");
                sources.add(moduleRoot + "/src/main/java");
                sources.add(moduleRoot + "/src/test/java");
                sources.add(moduleRoot + "/src");
            }
            return parse(file, sources, moduleName);
        }
        catch (IOException exception) {
            throw new InvocationTargetException(exception);
        }
        catch (SAXException exception) {
            throw new InvocationTargetException(exception);
        }
        catch (DocumentException exception) {
            throw new InvocationTargetException(exception);
        }
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
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException
     *             if the file could not be read
     * @throws SAXException
     *             if the file could not be read
     */
    public Collection<FileAnnotation> parse(final File file, final Collection<String> sources, final String moduleName)
            throws IOException, DocumentException, SAXException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            Map<String, String> hashToMessageMapping = createHashToMessageMapping(input);
            IOUtils.closeQuietly(input);

            input = new FileInputStream(file);
            return parse(input, sources, moduleName, hashToMessageMapping);
        }
        finally {
            IOUtils.closeQuietly(input);
        }
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
        digester.setClassLoader(FindBugsParser.class.getClassLoader());

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
        SortedBugCollection collection = new SortedBugCollection();
        collection.readXML(file);

        Project project = collection.getProject();
        for (String sourceFolder : sources) {
            project.addSourceDir(sourceFolder);
        }

        SourceFinder sourceFinder = new SourceFinder(project);
        String actualName = extractModuleName(moduleName, project);

        ArrayList<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
        Collection<BugInstance> bugs = collection.getCollection();
        for (BugInstance warning : bugs) {
            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();

            String message = warning.getMessage();
            if (message.contains("TEST: Unknown")) {
                message = FindBugsMessages.getInstance().getShortMessage(warning.getType(), LocaleProvider.getLocale());
            }
            Bug bug = new Bug(getPriority(warning),
                    StringUtils.defaultIfEmpty(hashToMessageMapping.get(warning.getInstanceHash()), message),
                    warning.getBugPattern().getCategory(),
                    warning.getType(), sourceLine.getStartLine(), sourceLine.getEndLine());
            bug.setInstanceHash(warning.getInstanceHash());

            if (!setCloudInformation(collection, warning, bug)) {
                bug.setNotAProblem(setCloudInformation(collection, warning, bug));
                bug.setFileName(findSourceFile(project, sourceFinder, sourceLine));
                bug.setPackageName(warning.getPrimaryClass().getPackageName());
                bug.setModuleName(actualName);
                setAffectedLines(warning, bug);

                annotations.add(bug);
            }
        }
        return annotations;
    }

    /**
     * Sets the cloud information.
     *
     * @param collection
     *            the warnings collection
     * @param warning
     *            the warning
     * @param bug
     *            the bug
     * @return true, if this warning is not a bug and should be ignored
     */
    private boolean setCloudInformation(final SortedBugCollection collection, final BugInstance warning, final Bug bug) {
        long firstSeen = collection.getCloud().getFirstSeen(warning);
        bug.setFirstSeen(firstSeen);
        int ageInDays = (int) ((System.currentTimeMillis() - firstSeen) / DAY_IN_MSEC);
        bug.setAgeInDays(ageInDays);
        bug.setReviewCount(collection.getCloud().getNumberReviewers(warning));

        return collection.getCloud().overallClassificationIsNotAProblem(warning);
    }

    private void setAffectedLines(final BugInstance warning, final Bug bug) {
        Iterator<BugAnnotation> annotationIterator = warning.annotationIterator();
        while (annotationIterator.hasNext()) {
            BugAnnotation bugAnnotation = annotationIterator.next();
            if (bugAnnotation instanceof SourceLineAnnotation) {
                SourceLineAnnotation annotation = (SourceLineAnnotation)bugAnnotation;
                bug.addLineRange(new LineRange(annotation.getStartLine(), annotation.getEndLine()));
            }
        }
    }

    private String findSourceFile(final Project project, final SourceFinder sourceFinder, final SourceLineAnnotation sourceLine) {
        try {
            SourceFile sourceFile = sourceFinder.findSourceFile(sourceLine);
            return sourceFile.getFullFileName();
        }
        catch (IOException exception) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Can't resolve absolute file name for file " + sourceLine.getSourceFile()
                    + ", dir list = " + project.getSourceDirList().toString());
            return sourceLine.getPackageName().replace(DOT, SLASH) + SLASH + sourceLine.getSourceFile();
        }
    }

    /**
     * Maps the FindBugs library rank to plug-in priority enumeration.
     *
     * @param warning
     *            the FindBugs warning
     * @return mapped priority enumeration
     */
    private Priority getPriority(final BugInstance warning) {
        int rank = warning.getBugRank();
        if (rank <= HIGH_PRIORITY_LOWEST_RANK) {
            return Priority.HIGH;
        }
        if (rank <= NORMAL_PRIORITY_LOWEST_RANK) {
            return Priority.NORMAL;
        }
        return Priority.LOW;
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
}

