package org.gozer.resources;

import org.gozer.GozerServer;
import org.junit.ClassRule;
import org.junit.Test;
import restx.tests.RestxSpecRule;

public class DependencyResourceSpecTest {
    @ClassRule
    public static RestxSpecRule rule = new RestxSpecRule(
            GozerServer.WEB_INF_LOCATION,
            GozerServer.WEB_APP_LOCATION);

    @Test
    public void should_get_all_the_dependencies() throws Exception {
        rule.runTest("specs/dependency/should_get_all_dependencies.spec.yaml");
    }

    @Test
    public void should_create_a_dependency() throws Exception {
        rule.runTest("specs/dependency/should_create_a_dependency.spec.yaml");
    }

    @Test
    public void should_resolve_a_dependency() throws Exception {
        rule.runTest("specs/dependency/should_resolve_a_dependency.spec.yaml");
    }
}