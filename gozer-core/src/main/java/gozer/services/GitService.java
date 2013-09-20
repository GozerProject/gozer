package gozer.services;

import gozer.GozerFactory;
import gozer.api.Cloner;
import gozer.model.Project;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;

@Component
public class GitService implements Cloner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);
    private String globalRepoPath;

    public GitService(@Named(GozerFactory.GLOBAL_REPO_PATH) String globalRepoPath) {
        this.globalRepoPath = globalRepoPath;
    }

    @Override
    public Project clone(Project project) {

        String localPath = globalRepoPath + "/" + project.getName();
        LOGGER.debug("local path of the cloned repository : [{}]", localPath);

        String remotePath = project.getScm();
        LOGGER.debug("URL of of the repository [{}]", remotePath);
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
        } catch(JGitInternalException e) {
            LOGGER.error("Project is already cloned", e);
        }

        project.setPath(localPath);
        project.setStatus(Project.Status.CLONED);
        LOGGER.info("Project {} has been cloned", project.getName());
        return project;
    }

    @Override
    public Project.Status getTo() {
        return Project.Status.CLONED;
    }
}
