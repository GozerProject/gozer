package org.gozer.services;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

import java.io.File;
import java.io.IOException;

public class GitService {

    public void cloneRepository() throws IOException, GitAPIException {

        String localPath = "target/git";
        String remotePath = "https://github.com/GozerProject/gozer.git";
        FileRepository localRepo = new FileRepository(localPath + "/.git");
        Git git = new Git(localRepo);

        Repository newRepo = new FileRepository(localPath + ".git");
        newRepo.create();

        Git.cloneRepository()
                .setURI(remotePath)
                .setDirectory(new File(localPath))
                .call();
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        GitService gitService = new GitService();
        gitService.cloneRepository();
    }
}
