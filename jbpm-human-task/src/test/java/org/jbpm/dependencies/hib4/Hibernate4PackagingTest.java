package org.jbpm.dependencies.hib4;

import java.io.File;
import java.util.*;

import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.jbpm.dependencies.PackagingTestCase;
import org.jbpm.dependencies.runner.JarTestRunner;

public class Hibernate4PackagingTest extends PackagingTestCase {

    /**
     * Build a ProcessBuilder that we can use to start the process which will run the test jar. 
     * 
     * @param testJarAbsolutePath The absolute path to the test jar. 
     * @param extraClassPathJars Extra jars that should be added to the class path.
     * @return A ProcessBuilder that can start the test-jar. 
     */
    protected static ProcessBuilder buildProcessBuilder(String testJarAbsolutePath, ArrayList<String> extraClassPathJars) {
        String javaHome = System.getProperties().getProperty("java.home");
        String sep = System.getProperties().getProperty("file.separator");
        String javaBin = javaHome + sep + "bin" + sep + "java";
    
        ArrayList<String> argList = new ArrayList<String>();
        argList.add(javaBin);
    
        { // add classpath
            ArrayList<String> classPathArgs = new ArrayList<String>();
            classPathArgs.add("-ea");
            classPathArgs.add("-classpath");
            
            StringBuilder classPath = new StringBuilder(testJarAbsolutePath);
            ArrayList<String> classpathJars = resolveJarsForClassPath();
            classpathJars.addAll(extraClassPathJars);
            for( String jarPath : classpathJars ) { 
                classPath.append(":" + jarPath);
            }
            classPathArgs.add(classPath.toString());
            
            argList.addAll(classPathArgs);
        }
        
        // "main" class to run
        argList.add(JarTestRunner.class.getCanonicalName());
        
        ProcessBuilder procBldr = new ProcessBuilder(argList);
        procBldr.redirectErrorStream(true);
    
        return procBldr;
    }

    /**
     * Resolve the jars needed to run any human-task tests. 
     * 
     * @return A list of the absolute paths to the jars (in the local maven repository). 
     */
    private static ArrayList<String> resolveJarsForClassPath() {
        ArrayList<File> dependencyFileList = new ArrayList<File>();
    
        // - Human-Task dependencies
        MavenDependencyResolver humanTaskResolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom(
                "pom.xml");
    
        humanTaskResolver.artifact("junit:junit");
    
        // human-task
        humanTaskResolver.artifact("org.jbpm:jbpm-workitems");
        humanTaskResolver.artifact("org.mvel:mvel2");
        humanTaskResolver.artifact("org.drools:drools-persistence-jpa").exclusion("dom4j:dom4j");
        humanTaskResolver.artifact("commons-collections:commons-collections");
    
        // logging
        humanTaskResolver.artifact("org.slf4j:slf4j-api");
        humanTaskResolver.artifact("org.slf4j:slf4j-log4j12");
    
        // persistence
        humanTaskResolver.artifact("org.hibernate.javax.persistence:hibernate-jpa-2.0-api");
        humanTaskResolver.artifact("com.h2database:h2");
        humanTaskResolver.artifact("org.codehaus.btm:btm");
    
        // other
        humanTaskResolver.artifact("org.subethamail:subethasmtp-wiser");
        
        File[] files = humanTaskResolver.resolveAsFiles();
        for (int f = 0; f < files.length; ++f) {
            if (files[f].getName().contains("dom4j")) {
                throw new RuntimeException("dom4j!");
            }
        }
    
        addArrayToList(files, dependencyFileList);
    
        // - Persistence dependencies
        MavenDependencyResolver hib4Resolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom(
                CLASSPATH_QUALIFIER + hib4PkgLoc + "pom.xml");
    
        hib4Resolver.artifact("org.hibernate:hibernate-core");
        hib4Resolver.artifact("org.hibernate:hibernate-entitymanager");
    
        addArrayToList(hib4Resolver.resolveAsFiles(), dependencyFileList);
    
        ArrayList<String> classPathList = new ArrayList<String>();
        for (File depFile : dependencyFileList) {
            classPathList.add(depFile.getAbsolutePath());
        }
    
        return classPathList;
    }

}
