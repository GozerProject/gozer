package org.gozer.resources;

import org.gozer.GozerServer;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxSpecRule;

public class ProjectResourceTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule(
            GozerServer.WEB_INF_LOCATION,
            GozerServer.WEB_APP_LOCATION);

    @Test
    public void should_get_all_the_projects() throws Exception {
        rule.runTest("specs/projects/should_get_all_projects.spec.yaml");
    }
}
