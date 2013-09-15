package gozer;

import com.google.common.base.Charsets;
import restx.SignatureKey;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

@Module
public class GozerModule {
    @Provides
    public SignatureKey signatureKey() {
        return new SignatureKey("5341679063568601943 gozer e372a614-d7a5-477b-9e3f-40c182b9f34d".getBytes(Charsets.UTF_8));
    }

    @Provides @Named(GozerFactory.GLOBAL_REPO_PATH)
    public String globalRepoPath() {
        return System.getProperty("global.repo.path", "target/git");
    }

    @Provides @Named(GozerFactory.DEPENDENCIES_REPOSITORY)
    public String dependenciesRepository() {
        return System.getProperty("dependencies.repositories", "target/repository");
    }

    @Provides @Named(GozerFactory.COMPILATION_DESTINATION)
    public String compilationDestination() {
        return System.getProperty("compilation.destination", "target/tmp/classes");
    }
}
