package hudson.plugins.findbugs.workflow;

import java.io.File;
import java.util.Set;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.FilePath;

import hudson.model.Result;

import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.findbugs.FindBugsResultAction;
import hudson.plugins.findbugs.FindBugsPublisher;

import static org.junit.Assert.*;

/**
 * Test workflow compatibility.
 */
public class WorkflowCompatibilityTest {

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    /**
     * Run a workflow job using {@link FindBugsPublisher} and check for success.
     */
    @Test
    public void findbugsPublisherWorkflowStep() throws Exception {
        j.jenkins.getInjector().injectMembers(this);
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf");
        File wfile = new File(j.jenkins.getRootDir() + "/workspace/wf/target");
        wfile.mkdirs();
        FilePath report = new FilePath(wfile).child("findbugs.xml");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream("/hudson/plugins/findbugs/parser/findbugs-native.xml"));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'FindBugsPublisher'])" +
        "}"));
        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
        FindBugsResultAction result = job.getLastBuild().getAction(FindBugsResultAction.class);
        assertTrue(result.getResult().getAnnotations().size() == 2);
    }

    /**
     * Run a workflow job using {@link FindBugsPublisher} with a failing threshols of 0, so the given example file
     * "/hudson/plugins/findbugs/parser/findbugs-native.xml" will make the build to fail.
     */
    @Test
    public void findbugsPublisherWorkflowStepSetLimits() throws Exception {
        j.jenkins.getInjector().injectMembers(this);
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf2");
        File wfile = new File(j.jenkins.getRootDir() + "/workspace/wf2/target");
        wfile.mkdirs();
        FilePath report = new FilePath(wfile).child("findbugsXml.xml");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream("/hudson/plugins/findbugs/parser/findbugs-native.xml"));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'FindBugsPublisher', pattern: '**/findbugsXml.xml', failedTotalAll: '0', usePreviousBuildAsReference: false])" +
        "}"));
        j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
    }

    /**
     * Run a workflow job using {@link FindBugsPublisher} with a unstable threshols of 0, so the given example file
     * "/hudson/plugins/findbugs/parser/findbugs-native.xml" will make the build to fail.
     */
    @Test
    public void findbugsPublisherWorkflowStepFailure() throws Exception {
        j.jenkins.getInjector().injectMembers(this);
        WorkflowJob job = j.jenkins.createProject(WorkflowJob.class, "wf3");
        File wfile = new File(j.jenkins.getRootDir() + "/workspace/wf3/target");
        wfile.mkdirs();
        FilePath report = new FilePath(wfile).child("findbugsXml.xml");
        report.copyFrom(WorkflowCompatibilityTest.class.getResourceAsStream("/hudson/plugins/findbugs/parser/findbugs-native.xml"));
        job.setDefinition(new CpsFlowDefinition(
        "node {" +
        "  step([$class: 'FindBugsPublisher', pattern: '**/findbugsXml.xml', unstableTotalAll: '0', usePreviousBuildAsReference: false])" +
        "}"));
        j.assertBuildStatus(Result.UNSTABLE, job.scheduleBuild2(0).get());
        FindBugsResultAction result = job.getLastBuild().getAction(FindBugsResultAction.class);
        assertTrue(result.getResult().getAnnotations().size() == 2);
    }
}
