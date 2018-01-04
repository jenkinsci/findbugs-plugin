package hudson.plugins.findbugs.parser; // NOPMD

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.jvnet.localizer.LocaleProvider;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.BugAnnotation;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.ba.SourceFile;
import edu.umd.cs.findbugs.ba.SourceFinder;
import edu.umd.cs.findbugs.cloud.Cloud;

import hudson.plugins.analysis.core.AnnotationParser;
import hudson.plugins.analysis.util.SecureDigester;
import hudson.plugins.analysis.util.TreeStringBuilder;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.LineRange;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.findbugs.FindBugsMessages;

/**
 * A parser for the native FindBugs XML files (ant task, batch file or maven-findbugs-plugin >= 1.2).
 *
 * @author Ulli Hafner
 */
// CHECKSTYLE:COUPLING-OFF
public class FindBugsParser implements AnnotationParser {
    /** Unique ID of this class. */
    private static final long serialVersionUID = 8306319007761954027L;

    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String CLOUD_DETAILS_URL_PROPERTY = "detailsUrl";
    private static final String EMPTY_STRING = "";

    private static final int DAY_IN_MSEC = 1000 * 60 * 60 * 24;
    private static final int HIGH_PRIORITY_LOWEST_RANK = 4;
    private static final int NORMAL_PRIORITY_LOWEST_RANK = 9;

    /** Collection of source folders. */
    @SuppressFBWarnings("SE")
    private final List<String> mavenSources = new ArrayList<String>();

    /** Determines whether to use the rank when evaluation the priority. @since 4.26 */
    private final boolean isRankActivated;

    private final Set<Pattern> excludePatterns = Sets.newHashSet();
    private final Set<Pattern> includePatterns = Sets.newHashSet();

    private boolean isFirstError = true;

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param isRankActivated
     *            determines whether to use the rank when evaluation the priority
     */
    public FindBugsParser(final boolean isRankActivated) {
        this(isRankActivated, EMPTY_STRING, EMPTY_STRING);
    }

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param isRankActivated
     *            determines whether to use the rank when evaluation the priority
     * @param excludePattern
     *            RegEx patterns of files to exclude from the report
     * @param includePattern
     *            RegEx patterns of files to include in the report
     */
    public FindBugsParser(final boolean isRankActivated, final String excludePattern, final String includePattern) {
        this(new ArrayList<String>(), isRankActivated, excludePattern, includePattern);
    }

    /**
     * Creates a new instance of {@link FindBugsParser}.
     *
     * @param sourceFolders
     *            a collection of folders to scan for source files. If empty, the source folders are guessed.
     * @param isRankActivated
     *            determines whether to use the rank when evaluation the priority
     * @param excludePattern
     *            RegEx patterns of files to exclude from the report
     * @param includePattern
     *            RegEx patterns of files to include in the report
     */
    public FindBugsParser(final Collection<String> sourceFolders, final boolean isRankActivated,
            final String excludePattern, final String includePattern) {
        mavenSources.addAll(sourceFolders);
        this.isRankActivated = isRankActivated;
        addPatterns(includePatterns, includePattern);
        addPatterns(excludePatterns, excludePattern);
    }

    /**
     * Add RegEx patterns to include/exclude in the report.
     *
     * @param patterns
     *            RegEx patterns
     * @param pattern
     *            String of RegEx patterns
     */
    private void addPatterns(final Set<Pattern> patterns, final String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            String[] split = StringUtils.split(pattern, ',');
            for (String singlePattern : split) {
                String trimmed = StringUtils.trim(singlePattern);
                String directoriesReplaced = StringUtils.replace(trimmed, "**", "*"); // NOCHECKSTYLE
                patterns.add(Pattern.compile(StringUtils.replace(directoriesReplaced, "*", ".*"))); // NOCHECKSTYLE
            }
        }
    }


    @Override
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
     * Returns the parsed FindBugs analysis file. This scanner accepts files in the native FindBugs format.
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
        return parse(new InputStreamProvider() {
            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
        }, sources, moduleName);
    }

    Collection<FileAnnotation> parse(final InputStreamProvider file, final Collection<String> sources,
            final String moduleName) throws IOException, DocumentException, SAXException {
        InputStream input = null;
        try {
            input = file.getInputStream();
            Map<String, String> hashToMessageMapping = new HashMap<String, String>();
            Map<String, String> categories = new HashMap<String, String>();
            for (XmlBugInstance bug : preParse(input)) {
                hashToMessageMapping.put(bug.getInstanceHash(), bug.getMessage());
                categories.put(bug.getType(), bug.getCategory());
            }
            IOUtils.closeQuietly(input);

            input = file.getInputStream();
            return parse(input, sources, moduleName, hashToMessageMapping, categories);
        }
        finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Pre-parses a file for some information not available from the FindBugs parser. Creates a mapping of FindBugs
     * warnings to messages. A bug is represented by its unique hash code. Also obtains original categories for bug
     * types.
     *
     * @param file
     *            the FindBugs XML file
     * @return the map of warning messages
     * @throws SAXException
     *             if the file contains no valid XML
     * @throws IOException
     *             signals that an I/O exception has occurred.
     */
    List<XmlBugInstance> preParse(final InputStream file) throws SAXException, IOException {
        SecureDigester digester = new SecureDigester(FindBugsParser.class);
        String rootXPath = "BugCollection/BugInstance";
        digester.addObjectCreate(rootXPath, XmlBugInstance.class);
        digester.addSetProperties(rootXPath);

        String fileXPath = rootXPath + "/LongMessage";
        digester.addCallMethod(fileXPath, "setMessage", 0);

        digester.addSetNext(rootXPath, "add", Object.class.getName());
        ArrayList<XmlBugInstance> bugs = new ArrayList<XmlBugInstance>();
        digester.push(bugs);
        digester.parse(file);

        return bugs;
    }

    /**
     * Returns the parsed FindBugs analysis file. This scanner accepts files in the native FindBugs format.
     *
     * @param file
     *            the FindBugs analysis file
     * @param sources
     *            a collection of folders to scan for source files
     * @param moduleName
     *            name of maven module
     * @param hashToMessageMapping
     *            mapping of hash codes to messages
     * @param categories
     *            mapping from bug types to their categories
     * @return the parsed result (stored in the module instance)
     * @throws IOException
     *             if the file could not be parsed
     * @throws DocumentException
     *             in case of a parser exception
     */
    private Collection<FileAnnotation> parse(final InputStream file, final Collection<String> sources,
            final String moduleName, final Map<String, String> hashToMessageMapping,
            final Map<String, String> categories) throws IOException, DocumentException {
        SortedBugCollection collection = readXml(file);

        Project project = collection.getProject();
        for (String sourceFolder : sources) {
            project.addSourceDir(sourceFolder);
        }

        SourceFinder sourceFinder = new SourceFinder(project);
        String actualName = extractModuleName(moduleName, project);

        TreeStringBuilder stringPool = new TreeStringBuilder();
        List<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
        Collection<BugInstance> bugs = collection.getCollection();

        for (BugInstance warning : bugs) {

            SourceLineAnnotation sourceLine = warning.getPrimarySourceLineAnnotation();

            String message = warning.getMessage();
            String type = warning.getType();
            if (message.contains("TEST: Unknown")) {
                message = FindBugsMessages.getInstance().getShortMessage(type, LocaleProvider.getLocale());
            }
            String category = categories.get(type);
            if (category == null) { // alternately, only if warning.getBugPattern().getType().equals("UNKNOWN")
                category = warning.getBugPattern().getCategory();
            }
            Bug bug = new Bug(getPriority(warning), StringUtils.defaultIfEmpty(
                    hashToMessageMapping.get(warning.getInstanceHash()), message), category, type,
                    sourceLine.getStartLine(), sourceLine.getEndLine());
            bug.setInstanceHash(warning.getInstanceHash());
            bug.setRank(warning.getBugRank());

            boolean ignore = setCloudInformation(collection, warning, bug);
            if (!ignore) {
                bug.setNotAProblem(false);
                bug.setFileName(findSourceFile(project, sourceFinder, sourceLine));
                bug.setPackageName(warning.getPrimaryClass().getPackageName());
                bug.setModuleName(actualName);
                setAffectedLines(warning, bug);

                annotations.add(bug);
                bug.intern(stringPool);
            }

        }

        return applyFilters(annotations);
    }


    /**
     * Applies the exclude and include filters to the found annotations.
     *
     * @param allAnnotations
     *            all annotations
     * @return the filtered annotations if there is a filter defined
     */
    private List<FileAnnotation> applyFilters(final List<FileAnnotation> allAnnotations) {
        List<FileAnnotation> includedAnnotations;
        if (includePatterns.isEmpty()) {
            includedAnnotations = allAnnotations;
        }
        else {
            includedAnnotations = new ArrayList<FileAnnotation>();
            for (FileAnnotation annotation : allAnnotations) {
                for (Pattern include : includePatterns) {
                    if (include.matcher(annotation.getFileName()).matches()) {
                        includedAnnotations.add(annotation);
                    }
                }
            }
        }
        if (excludePatterns.isEmpty()) {
            return includedAnnotations;
        }
        else {
            List<FileAnnotation> excludedAnnotations = new ArrayList<FileAnnotation>(includedAnnotations);
            for (FileAnnotation annotation : includedAnnotations) {
                for (Pattern exclude : excludePatterns) {
                    if (exclude.matcher(annotation.getFileName()).matches()) {
                        excludedAnnotations.remove(annotation);
                    }
                }
            }
            return excludedAnnotations;
        }
    }

    private Priority getPriority(final BugInstance warning) {
        if (isRankActivated) {
            return getPriorityByRank(warning);
        }
        else {
            return getPriorityByPriority(warning);
        }
    }

    private SortedBugCollection readXml(final InputStream file) throws IOException, DocumentException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(FindBugsParser.class.getClassLoader());
            SortedBugCollection collection = new SortedBugCollection();
            collection.readXML(file);
            return collection;
        }
        finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
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
    @SuppressFBWarnings("NP")
    private boolean setCloudInformation(final SortedBugCollection collection, final BugInstance warning, final Bug bug) {
        Cloud cloud = collection.getCloud();
        cloud.waitUntilIssueDataDownloaded();

        bug.setShouldBeInCloud(cloud.isOnlineCloud());
        Map<String, String> cloudDetails = collection.getXmlCloudDetails();
        bug.setDetailsUrlTemplate(cloudDetails.get(CLOUD_DETAILS_URL_PROPERTY));

        long firstSeen = cloud.getFirstSeen(warning);
        bug.setInCloud(cloud.isInCloud(warning));
        bug.setFirstSeen(firstSeen);
        int ageInDays = (int)((collection.getAnalysisTimestamp() - firstSeen) / DAY_IN_MSEC);
        bug.setAgeInDays(ageInDays);
        bug.setReviewCount(cloud.getNumberReviewers(warning));

        return cloud.overallClassificationIsNotAProblem(warning);
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

    private String findSourceFile(final Project project, final SourceFinder sourceFinder,
            final SourceLineAnnotation sourceLine) {
        try {
            SourceFile sourceFile = sourceFinder.findSourceFile(sourceLine);
            return sourceFile.getFullFileName();
        }
        catch (IOException exception) {
            StringBuilder sb = new StringBuilder("Can't resolve absolute file name for file ");
            sb.append(sourceLine.getSourceFile());
            if (isFirstError) {
                sb.append(", dir list = ");
                sb.append( project.getSourceDirList());
                isFirstError = false;
            }
            Logger.getLogger(getClass().getName()).log(Level.WARNING, sb.toString());
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
    private Priority getPriorityByRank(final BugInstance warning) {
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
     * Maps the FindBugs library priority to plug-in priority enumeration.
     *
     * @param warning
     *            the FindBugs warning
     * @return mapped priority enumeration
     */
    private Priority getPriorityByPriority(final BugInstance warning) {
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
     * Extracts the module name from the specified project. If empty then the provided default name is used.
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
     * Provides an input stream for the parser.
     */
    interface InputStreamProvider {
        InputStream getInputStream() throws IOException;
    }
}
