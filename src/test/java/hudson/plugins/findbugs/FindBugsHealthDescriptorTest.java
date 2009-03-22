package hudson.plugins.findbugs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.plugins.findbugs.util.AbstractHealthDescriptor;
import hudson.plugins.findbugs.util.AbstractHealthDescriptorTest;
import hudson.plugins.findbugs.util.HealthDescriptor;
import hudson.plugins.findbugs.util.NullHealthDescriptor;
import hudson.plugins.findbugs.util.model.AnnotationProvider;

import org.junit.Test;
import org.jvnet.localizer.Localizable;

/**
 * Tests the class {@link FindBugsHealthDescriptor}.
 *
 * @author Ulli Hafner
 */
public class FindBugsHealthDescriptorTest extends AbstractHealthDescriptorTest {
    /**
     * Verifies the different messages if the number of items are 0, 1, and 2.
     */
    @Test
    public void verifyNumberOfItems() {
        AnnotationProvider provider = mock(AnnotationProvider.class);
        FindBugsHealthDescriptor healthDescriptor = new FindBugsHealthDescriptor(NullHealthDescriptor.NULL_HEALTH_DESCRIPTOR);

        Localizable description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.FindBugs_ResultAction_HealthReportNoItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(1);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.FindBugs_ResultAction_HealthReportSingleItem(), description.toString());

        when(provider.getNumberOfAnnotations()).thenReturn(2);
        description = healthDescriptor.createDescription(provider);
        assertEquals(Messages.FindBugs_ResultAction_HealthReportMultipleItem(2), description.toString());
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractHealthDescriptor createHealthDescriptor(final HealthDescriptor healthDescriptor) {
        return new FindBugsHealthDescriptor(healthDescriptor);
    }
}

