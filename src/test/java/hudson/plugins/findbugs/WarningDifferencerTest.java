package hudson.plugins.findbugs;

import static org.junit.Assert.*;

import org.junit.Test;

public class WarningDifferencerTest {
    @Test
    public void testWarningEquals() {
        Warning first = new Warning();
        first.setType("type1");
        first.setClassname("type1");
        first.setLineNumber("type1");

        Warning second = new Warning();
        second.setType("type1");
        second.setClassname("type1");
        second.setLineNumber("type1");

        assertEquals(first, second);
        second.setLineNumber("");
        assertFalse(first.equals(second));
    }

    @Test
    public void testDifferencer() {
        Module actual = new Module();
        Module previous = new Module();

        Warning warning = new Warning();
        warning.setType("type1");
        warning.setLineNumber("type1");
        JavaClass javaClass = new JavaClass();
        javaClass.setClassname("findbugs.Class");
        javaClass.addWarning(warning);
        actual.addClass(javaClass);

        warning = new Warning();
        warning.setType("type1");
        warning.setLineNumber("type1");
        javaClass = new JavaClass();
        javaClass.setClassname("findbugs.Class");
        javaClass.addWarning(warning);
        previous.addClass(javaClass);

        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setType("type2");
        warning.setLineNumber("type1");
        javaClass = new JavaClass();
        javaClass.setClassname("findbugs.Class");
        javaClass.addWarning(warning);
        previous.addClass(javaClass);

        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(1, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setType("type2");
        warning.setLineNumber("type1");
        javaClass = new JavaClass();
        javaClass.setClassname("findbugs.Class");
        javaClass.addWarning(warning);
        actual.addClass(javaClass);

        assertEquals(0, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());

        warning = new Warning();
        warning.setType("type3");
        warning.setLineNumber("type1");
        javaClass = new JavaClass();
        javaClass.setClassname("findbugs.Class");
        javaClass.addWarning(warning);
        actual.addClass(javaClass);

        assertEquals(1, WarningDifferencer.getNewWarnings(actual, previous).size());
        assertEquals(0, WarningDifferencer.getFixedWarnings(actual, previous).size());
    }
}

