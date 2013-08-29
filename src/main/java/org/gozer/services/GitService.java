package org.gozer.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gozer.GozerFactory;
import org.gozer.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;

import static org.gozer.model.Project.Status.CLONED;

@Component
public class GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);
    private String globalRepoPath;

    public GitService(@Named(GozerFactory.GLOBAL_REPO_PATH) String globalRepoPath) {
        this.globalRepoPath = globalRepoPath;
    }

    public Project cloneRepository(Project project) {

        String localPath = globalRepoPath + "/" + project.getName();
        String remotePath = project.getScm();
//        FileRepository localRepo = new FileRepository(localPath + "/.git");
//        Git git = new Git(localRepo);


        // TODO sert à quelque chose dans le cas d'un clone ?
//        Repository newRepo = new FileRepository(localPath + "/.git");
//        newRepo.create();

        try {
            Git.cloneRepository()
                    .setURI(remotePath)
                    .setDirectory(new File(localPath))
                    .call();
        } catch (GitAPIException e) {
            LOGGER.error("Error during cloning the repository", e);
        }

        project.setPath(localPath);
        project.setStatus(CLONED);

        return project;
    }

}
