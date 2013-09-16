package org.gozer.services;

import org.junit.Test;
import org.kevoree.kcl.KevoreeJarClassLoader;
import org.kevoree.resolver.MavenResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class KCLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KCLTest.class);

    @Test
    public void should_load_classe() throws MalformedURLException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        KevoreeJarClassLoader kclScope1 = new KevoreeJarClassLoader();
        kclScope1.isolateFromSystem();

        KevoreeJarClassLoader kclScope = new KevoreeJarClassLoader();

        MavenResolver resolver = new MavenResolver();
        resolver.setBasePath("target/repository");

        File dependencyPath = resolver.resolve("mvn:org.kevoree.kcl:org.kevoree.kcl:1:jar", Arrays.asList("http://repo1.maven.org/maven2"));

        kclScope.addJarFromURL(dependencyPath.toURI().toURL());
//        kclScope.add(dependencyPath.toURI().toURL());

//        kclScope.add(this.getClass().getClassLoader().getResourceAsStream("org.kevoree.kcl.jar"));

//        LOGGER.info("deploiement jar path : {}", dependencyPath.toURI().toURL());
//        kclScope1.addChild(kclScope);

        Class resolvedClass = kclScope.loadClass("org.kevoree.kcl.KevoreeJarClassLoader");
//        Class cl1 = kclScope.loadClass("org.springframework.core.io.Resource");
        Object objOfCl1 = resolvedClass.newInstance();
        assertThat(objOfCl1).isInstanceOf(KevoreeJarClassLoader.class);
//        KevoreeJarClassLoader kclScope2 = new KevoreeJarClassLoader();
//
//        //can be call dynamicallly at anytime
//        //removeChild also available
//        kclScope2.addChild(kclScope1);
//
//        Class clMain = kclScope2.loadClass("yourMain");
//        //slf4j classes will be resolved transitively
//        clMain.getMethod("main").invoke();
    }
}
