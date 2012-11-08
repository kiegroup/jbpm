package org.jbpm.dependencies.hib4;

import static junit.framework.Assert.fail;

import java.io.*;
import java.util.*;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Ignore;
import org.junit.Test;

public class Hib4PostgresPackagingTest extends Hibernate4PackagingTest {

    @Test
//    @Ignore
    public void test() throws Exception {
        
        File humanTaskJar = buildPostgreSQLJpa2Hiberate4Jar();

        // Build and start process
        ProcessBuilder procBldr = buildProcessBuilder(humanTaskJar.getAbsolutePath(), resolvePostgreSQLJarsForClassPath());
        Process testProcess = procBldr.start();
        
        // Print output
        BufferedReader in = new BufferedReader(new InputStreamReader(testProcess.getInputStream()));
        String line = null;
        while((line = in.readLine()) != null) {
          System.out.println(line);
        }

        // Wait for termination
        int exitValue = testProcess.waitFor(); 
        if(exitValue != 0 ) { 
           fail("Tests did not succeed [ exit == " + exitValue + "]");
        }
        
    }

    /**
     * This method builds a jar that contains the human-task classes (including tests). 
     * @return
     */
    private File buildPostgreSQLJpa2Hiberate4Jar() throws IOException {
        String fileName = "h2Hib4.jar";
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, fileName);
    
        // Resources
        HashMap<String, String> targetFileMap = new HashMap<String, String>();
        targetFileMap.put("META-INF/persistence.xml", jpa2PkgLoc + "postgresql/persistence.xml");
        targetFileMap.put("datasource.properties", jpa2PkgLoc + "postgresql/datasource.properties");
        targetFileMap.put("META-INF/Taskorm-JPA2.xml", jpa2PkgLoc + TASKORM_JPA2_XML);
        targetFileMap.put("META-INF/I18nText.hbm.xml", jpa2PkgLoc + "postgresql/I18nText.hbm.xml");
        
        targetFileMap.put("log4j.xml", hib4PkgLoc + "log4j.xml");
        targetFileMap.put(userGroupCallbackProperties, userGroupCallbackProperties);
        Set<String> mvelFiles = getMvelFilePaths();
        for( String mvelFile : mvelFiles ) { 
            targetFileMap.put(mvelFile, mvelFile);
        }
        
        // Classes
        archive.addPackages(true, "org.jbpm");
    
        for (String targetPath : targetFileMap.keySet()) {
            // .addAsResource( resource, target )
            archive.addAsResource(targetFileMap.get(targetPath), ArchivePaths.create(targetPath));
        }
    
        File testPackage = new File(packageTargetDir, fileName);
        archive.as(ZipExporter.class).exportTo(testPackage, true);
    
        return testPackage;
    }

    private ArrayList<String> resolvePostgreSQLJarsForClassPath() {
        MavenDependencyResolver postgresResolver = DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom(
                CLASSPATH_QUALIFIER + jpa2PkgLoc + "postgresql/pom.xml");
                
        postgresResolver.artifact("postgresql:postgresql");
        
        ArrayList<String> classPathList = new ArrayList<String>();
        File [] h2Files = postgresResolver.resolveAsFiles(); 
        for(int f = 0; f < h2Files.length; ++f ) { 
            classPathList.add(h2Files[f].getAbsolutePath());
        }
        
        return classPathList;
    }

    // ============
    //
    // C  R  U  F  T
    //
    // ============

    
}
