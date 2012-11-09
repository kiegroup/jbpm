package org.jbpm.dependencies.hib4;

import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static junit.framework.Assert.fail;

/**
 * To properly run it it requires oracle driver to be given as system property:
 * -Doracle.jdbc.path=<Absolute_path_to_driver>
 */
public class Hib4OraclePackagingTest extends Hibernate4PackagingTest {

    @Test
    @Ignore
    public void test() throws Exception {
        
        File humanTaskJar = buildOracleJpa2Hiberate4Jar();

        // Build and start process
        ProcessBuilder procBldr = buildProcessBuilder(humanTaskJar.getAbsolutePath(), resolveOracleJarsForClassPath());
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
    private File buildOracleJpa2Hiberate4Jar() throws IOException {
        String fileName = "h2Hib4.jar";
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, fileName);
    
        // Resources
        HashMap<String, String> targetFileMap = new HashMap<String, String>();
        targetFileMap.put("META-INF/persistence.xml", jpa2PkgLoc + "oracle/persistence.xml");
        targetFileMap.put("datasource.properties", jpa2PkgLoc + "oracle/datasource.properties");
        targetFileMap.put("META-INF/Taskorm-JPA2.xml", jpa2PkgLoc + TASKORM_JPA2_XML);

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

    private ArrayList<String> resolveOracleJarsForClassPath() {
        String oracleDriverPath = System.getProperty("oracle.jdbc.path");
        if (oracleDriverPath == null) {
            throw new IllegalArgumentException("Oracle driver is not provided, please specify it via system property: oracle.jdbc.path");
        }
        ArrayList<String> classPathList = new ArrayList<String>();
        File [] h2Files = new File[] {new File(oracleDriverPath)};
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
