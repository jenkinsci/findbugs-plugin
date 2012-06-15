package hudson.plugins.findbugs.parser;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Efficient representation for mostly hexadecimal strings.
 *
 * If the string is "([0-9a-f][0-9a-f])*", this class internally uses byte[] to represent
 * the string in the most compact form. At the same time, the class is designed not to lose
 * any information in the original string, so if the input does not fit this pattern,
 * original string is retained. In the test data, I noted some small number of hex strings that are 31 letter long,
 * and in any case assuming that the instance hash is hex string is a dangerous assumption anyway.
 *
 * <p>
 * In either case, {@code new HexishString(x).toString().equals(x)} for any non-null string.
 *
 * <p>
 * This class is developed to represent "instance hash" that findbugs produces, which appears
 * to be 128bit information encoded in the hex form. In String with associated char[], it takes up about 128 bytes,
 * but in this form it only occupies about 64 bytes if it's hex, and 112 when it's not. So it's more space efficient
 * either way.
 *
 * @author Kohsuke Kawaguchi
 */
public final class HexishString {
    private final Object content;

    public HexishString(String value) {
        if (isHex(value)) {
            try {
                content = Hex.decodeHex(value.toCharArray());
            } catch (DecoderException e) {
                throw new AssertionError(e);    // we've already verified that value is a valid hex
            }
        } else
            this.content = value.toCharArray();
    }

    @Override
    public String toString() {
        if (content instanceof char[])
            return new String((char[])content);
        else
            return Hex.encodeHexString((byte[])content);
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof HexishString && this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    private boolean isHex(String value) {
        int len = value.length();
        if (len%2==1)   return false;   // if it's odd number of hex they don't map unambiguously to byte[].
        for (int i= len -1; i>=0; i--) {
            char ch = value.charAt(i);
            if (('0'<=ch && ch<='9') || ('a'<=ch && ch<='f'))
                continue;
            else
                return false;
        }
        return true;
    }

    public static HexishString of(String s) {
        return s==null ? null : new HexishString(s);
    }

    /**
     * {@link Converter} implementation for XStream that writes this out as a plain string.
     */
    public static final class ConverterImpl extends AbstractSingleValueConverter {
        public ConverterImpl(XStream xs) {
        }

        @Override
        public Object fromString(String str) {
            return new HexishString(str);
        }

        public boolean canConvert(Class type) {
            return type==HexishString.class;
        }
    }
}
