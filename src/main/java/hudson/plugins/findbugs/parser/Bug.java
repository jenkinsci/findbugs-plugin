package hudson.plugins.findbugs.parser;

import hudson.plugins.analysis.util.model.AbstractAnnotation;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.findbugs.FindBugsMessages;
import hudson.plugins.findbugs.Messages;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jvnet.localizer.LocaleProvider;

/**
 * A serializable Java Bean class representing a warning.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * </p>
 *
 * @author Ulli Hafner
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public class Bug extends AbstractAnnotation {
    private static final long serialVersionUID = 5171661552905752370L;
    /** Origin of the annotation. */
    private static final String ORIGIN = "findbugs";

    private String tooltip = StringUtils.EMPTY;

    /** Unique hash code of this bug. */
    private String instanceHash;
    /** Computed from firstSeen. */
    private int ageInDays;
    private long firstSeen;
    private int reviewCount;
    private boolean notAProblem;
    private boolean inCloud;

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     */
    public Bug(final Priority priority, final String message, final String category, final String type,
            final int start, final int end) {
        super(priority, message, start, end, category, type);

        setOrigin(ORIGIN);
    }

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param lineNumber
     *            the line number of the warning in the corresponding file
     */
    public Bug(final Priority priority, final String message, final String category, final String type, final int lineNumber) {
        this(priority, message, category, type, lineNumber, lineNumber);
    }

    /**
     * Creates a new instance of <code>Bug</code> that has no associated line in code (file warning).
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     */
    public Bug(final Priority priority, final String message, final String category, final String type) {
        this(priority, message, category, type, 0, 0);
    }

    /**
     * Creates a new instance of <code>Bug</code>.
     *
     * @param priority
     *            the priority
     * @param message
     *            the message of the warning
     * @param category
     *            the warning category
     * @param type
     *            the identifier of the warning type
     * @param start
     *            the first line of the line range
     * @param end
     *            the last line of the line range
     * @param tooltip
     *            the tooltip to show
     */
    public Bug(final Priority priority, final String message, final String category, final String type,
            final int start, final int end, final String tooltip) {
        this(priority, message, category, type, start, end);

        this.tooltip = tooltip;
    }

    // CHECKSTYLE:OFF Properties of FindBugs cloud
    public long getFirstSeen() {
        return firstSeen;
    }

    void setFirstSeen(final long firstSeen) {
        this.firstSeen = firstSeen;
    }

    public void setInCloud(final boolean inCloud) {
        this.inCloud = inCloud;
    }

    public boolean isInCloud() {
        return inCloud;
    }

    public int getAgeInDays() {
        return ageInDays;
    }

    void setAgeInDays(final int ageInDays) {
        this.ageInDays = ageInDays;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    void setReviewCount(final int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public boolean isNotAProblem() {
        return notAProblem;
    }

    void setNotAProblem(final boolean notAProblem) {
        this.notAProblem = notAProblem;
    }
    // CHECKSTYLE:ON

    /**
     * Rebuilds the priorities mapping.
     *
     * @return the created object
     */
    private Object readResolve() {
        if (instanceHash == null) {
            instanceHash = String.valueOf(super.hashCode());
        }

        return this;
    }

    /** {@inheritDoc} */
    public String getToolTip() {
        return StringUtils.defaultIfEmpty(tooltip, FindBugsMessages.getInstance().getMessage(getType(), LocaleProvider.getLocale()));
    }

    @Override
    public String getMessage() {
        return super.getMessage() + getCloudInformation();
    }

    private String getCloudInformation() {
        StringBuilder cloudMessage = new StringBuilder();
        if (ageInDays == 1) {
            cloudMessage.append(Messages.FindBugs_Bug_cloudInfo_seenAt_singular());
        }
        else if (ageInDays > 1) {
            cloudMessage.append(Messages.FindBugs_Bug_cloudInfo_seenAt_plural(ageInDays));
            if (firstSeen > 0) {
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                cloudMessage.append(" ");
                cloudMessage.append(Messages.FindBugs_Bug_cloudInfo_firstSeen(format.format(new Date(firstSeen))));
            }
        }

        if (reviewCount > 0) {
            if (cloudMessage.length() > 0) {
                cloudMessage.append(" - ");
            }
            if (reviewCount == 1) {
                cloudMessage.append(Messages.FindBugs_Bug_cloudInfo_reviewer_singular());
            }
            else {
                cloudMessage.append(Messages.FindBugs_Bug_cloudInfo_reviewer_plural(reviewCount));
            }
        }
        if (cloudMessage.length() == 0) {
            return StringUtils.EMPTY;
        }
        return "<br/><br/>"
                + "<img src='/plugin/findbugs/icons/fb-cloud-icon-small.png' "
                + "title=\"" + Messages.FindBugs_Bug_cloudInfo_title() + "\"/> "
                + cloudMessage.toString();
    }

    /**
     * Sets the unique hash code of this bug.
     *
     * @param instanceHash the instance hash as generated by the FindBugs library
     */
    public void setInstanceHash(final String instanceHash) {
        this.instanceHash = instanceHash;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 31 + ((instanceHash == null) ? 0 : instanceHash.hashCode()); // NOCHECKSTYLE
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Bug other = (Bug)obj;
        if (instanceHash == null) {
            if (other.instanceHash != null) {
                return false;
            }
        }
        else if (!instanceHash.equals(other.instanceHash)) {
            return false;
        }
        return true;
    }
}

