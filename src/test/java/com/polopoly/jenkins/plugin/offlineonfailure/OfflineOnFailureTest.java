package com.polopoly.jenkins.plugin.offlineonfailure;

import org.jvnet.hudson.test.HudsonTestCase;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import hudson.tasks.Builder;
import hudson.tasks.Shell;

public class OfflineOnFailureTest
    extends HudsonTestCase
{
    private static final Builder FAILURE_SHELL_BUILDER = new Shell("exit 1");
    private static final Builder SUCCESS_SHELL_BUILDER = new Shell("exit 0");

    public void test_node_is_not_taken_offline_on_success()
        throws Exception
    {
        performBuildAndAssertNodeStatus(SUCCESS_SHELL_BUILDER, "not_master", false, false);
    }

    public void test_node_is_taken_offline_on_success_if_alwaysOffline_is_true()
            throws Exception
    {
        performBuildAndAssertNodeStatus(SUCCESS_SHELL_BUILDER, "not_master", true, true);
    }

    public void test_node_is_taken_offline_on_failure()
        throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "not_master", true, false);
    }

    public void test_master_node_is_not_taken_offline_on_failure()
        throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "master", false, false);
    }

    public void test_master_node_is_not_taken_offline_on_failure_even_if_always()
            throws Exception
    {
        performBuildAndAssertNodeStatus(FAILURE_SHELL_BUILDER, "master", false, true);
    }


    private void performBuildAndAssertNodeStatus(final Builder builder,
                                                 final String label,
                                                 final boolean nodeIsExpectedToBeTakenOffline,
                                                 final boolean alwaysOffline)
        throws Exception
    {
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(builder);

        OfflineOnFailurePublisher publisher = new OfflineOnFailurePublisher(alwaysOffline);

        project.getPublishersList().add(publisher);

        if (label != "master") {
            createOnlineSlave(Label.get("not_master"));
        }
        project.setAssignedLabel(Label.get(label));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals("Node state after build was not the expected one!",
                     nodeIsExpectedToBeTakenOffline,
                     build.getBuiltOn().toComputer().isOffline());
    }
}
