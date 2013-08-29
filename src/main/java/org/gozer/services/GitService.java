package org.gozer.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gozer.model.Project;

import java.io.File;
import java.io.IOException;

public class GitService {

    private static final String GLOBAL_REPO_PATH = "target/git";

    public void cloneRepository(Project project) throws IOException, GitAPIException {

        String localPath = GLOBAL_REPO_PATH + "/" + project.getName();
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
