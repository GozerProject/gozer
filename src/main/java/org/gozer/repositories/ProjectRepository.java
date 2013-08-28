package org.gozer.repositories;

import com.google.common.annotations.VisibleForTesting;
import org.gozer.model.Project;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component()
public class ProjectRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRepository.class);

    private Map<Long,Project> projects;
    private Long index;
    private DB db;

    public ProjectRepository() {
        LOGGER.info("Created");
        index = 0L;

        // configure and open database using builder pattern.
        // all options are available with code auto-completion.
        db = DBMaker.newFileDB(new File("testdb"))
                .closeOnJvmShutdown()
//                .encryptionEnable("password")
                .make();

// open existing an collection (or create new)
        projects = db.getTreeMap("collectionName");
        LOGGER.info("projects : {}", projects);
     }

    public Collection<Project> getAll() {
        return projects.values();
    }

    public Project create(Project project) {
        Project newProject = new Project(project);
        newProject.setId(++index);
        projects.put(newProject.getId(), newProject);
        db.commit();
        return newProject;
    }

    @VisibleForTesting
    void setProjects(Map<Long, Project> projects) {
        this.projects = projects;
    }

    @VisibleForTesting
    Map<Long, Project> getProjects() {
        return projects;
    }

    public Project findByName(String name) {

        checkNotNull(name);

        for (Project project : projects.values()) {
            if (name.equals(project.getName())) {
                return project;
            }
        }

        return null;
    }
}
