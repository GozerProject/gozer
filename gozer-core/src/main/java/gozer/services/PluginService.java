package gozer.services;

import gozer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static gozer.model.builders.ProjectBuilder.aProject;

@Component
public class PluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);

    public PluginService() {
    }

    public Set<Project> findAllPlugins() {
        return newHashSet(aProject().withName("gozer-rest").build(),
                          aProject().withName("gozer-admin-web").build());
    }


}
