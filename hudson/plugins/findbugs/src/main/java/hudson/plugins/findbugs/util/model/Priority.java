// CHECKSTYLE:OFF
package hudson.plugins.findbugs.util.model;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * Defines the priority of an annotation.
 *
 * @author Ulli Hafner
 */
public enum Priority {
    HIGH, NORMAL, LOW;

    /**
     * Converts a String priority to an actual enumeration value.
     *
     * @param priority
     *            priority as a String
     * @return enumeration value.
     */
    public static Priority fromString(final String priority) {
        return Priority.valueOf(StringUtils.upperCase(priority));
    }

    /**
     * Converts priorities for {@link XStream} deserialization.
     */
    public static final class PriorityConverter extends AbstractSingleValueConverter {
        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public boolean canConvert(final Class type) {
            return type.equals(Priority.class);
        }

        /** {@inheritDoc} */
        @Override
        public Object fromString(final String str) {
            return Priority.valueOf(str);
        }
    }
}