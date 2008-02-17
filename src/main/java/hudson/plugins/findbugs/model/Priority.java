// CHECKSTYLE:OFF
package hudson.plugins.findbugs.model;

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