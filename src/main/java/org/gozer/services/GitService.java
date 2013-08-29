package org.gozer.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gozer.GozerFactory;
import org.gozer.model.Project;
import restx.factory.Component;

import javax.inject.Named;
import java.io.File;
import java.io.IOException;

@Component
public class GitService {

    private String globalRepoPath;

    public GitService(@Named(GozerFactory.GLOBAL_REPO_PATH) String globalRepoPath) {
        this.globalRepoPath = globalRepoPath;
    }

    public void cloneRepository(Project project) throws IOException, GitAPIException {

        String localPath = globalRepoPath + "/" + project.getName();
        String remotePath = project.getScm();
//        FileRepository localRepo = new FileRepository(localPath + "/.git");
//        Git git = new Git(localRepo);


        // TODO sert à quelque chose dans le cas d'un clone ?
//        Repository newRepo = new FileRepository(localPath + "/.git");
//        newRepo.create();

        Git.cloneRepository()
                .setURI(remotePath)
                .setDirectory(new File(localPath))
                .call();
    }

//    public static void main(String[] args) throws IOException, GitAPIException {
//        Project project = aProject().withName("spring-pet-clinic")
//                                    .withScm("https://github.com/SpringSource/spring-petclinic.git")
//                                    .build();
//
//        GitService gitService = new GitService();
//        gitService.cloneRepository(project);
//    }
}
