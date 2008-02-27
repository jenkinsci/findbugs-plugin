package hudson.plugins.findbugs;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest {
    @Test
    public void testResultOk() {
        FindBugsResult result = createMock(FindBugsResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(0);
        expect(result.getNumberOfModules()).andReturn(1);

        replay(result);

        ResultSummary summary = new ResultSummary(result);
        Assert.assertEquals("FindBugs: no warnings have been found in 1 FindBugs files.",
                summary.getMessage());

        verify(result);
    }
}

