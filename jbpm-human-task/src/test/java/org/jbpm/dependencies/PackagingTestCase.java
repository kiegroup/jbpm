/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009-2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.jbpm.dependencies;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;

import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;

/**
 * @author Hardy Ferentschik 
 * 
 * (Original author 
 *  of hibernate-entitymanager/org.hibernate.jpa.test.packaging.PackagingTestCase 
 *  at https://github.com/hibernate/hibernate-orm )
 * 
 * Modified for use by the jBPM project.
 */
public abstract class PackagingTestCase {

    protected static ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    protected static ClassLoader bundleClassLoader;
    protected static File packageTargetDir;

    protected final static String CLASSPATH_QUALIFIER = "classpath:";
    protected final static String FILE_QUALIFIER = "file:";
    
    protected final static String hib4PkgLoc = "package/hib4/";
    protected final static String jpa2PkgLoc = "package/jpa2/";
    
    protected final static String TASKORM_JPA2_XML = "Taskorm-JPA2.xml";
    protected final static String PERSISTENCE_XML = "persistence.xml";
    protected final static String DATASOURCE_PROPERTIES = "datasource.properties";
    protected final static String userGroupCallbackProperties  = "jbpm.usergroup.callback.properties";

    static {
        // get a URL reference to something we now is part of the classpath (us)
        URL myUrl = originalClassLoader.getResource(PackagingTestCase.class.getName().replace('.', '/') + ".class");
        int index;
        String classPathBase = "target";
        if (myUrl.getFile().contains(classPathBase)) {
            // assume there's normally a /target
            index = myUrl.getFile().lastIndexOf(classPathBase);
        } else {
            classPathBase = "bin";
            // if running in some IDEs, may be in /bin instead
            index = myUrl.getFile().lastIndexOf(classPathBase);
        }

        if (index == -1) {
            fail("Unable to setup packaging test");
        }

        String baseDirPath = myUrl.getFile().substring(0, index);
        File baseDir = new File(baseDirPath);

        File testPackagesDir = new File(baseDir, classPathBase + "/package/");
        try {
            bundleClassLoader = new URLClassLoader(new URL[] { testPackagesDir.toURL() }, originalClassLoader);
        } catch (MalformedURLException e) {
            fail("Unable to build custom class loader");
        }
        
        packageTargetDir = new File(baseDir, classPathBase + "/test-jars");
        packageTargetDir.mkdirs();
    }

    @Before
    public void prepareTCCL() {
        // add the bundle class loader in order for ShrinkWrap to build the test package
        Thread.currentThread().setContextClassLoader(bundleClassLoader);
    }

    @After
    public void resetTCCL() throws Exception {
        // reset the classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    protected static <T> void addArrayToList(T[] array, ArrayList<T> list) {
        for (int i = 0; i < array.length; ++i) {
            list.add(array[i]);
        }
    }

    /**
     * Retrieve all MVEL files needed to run the tests. 
     * 
     * @return 
     * 
     * @throws IOException
     */
    protected static Set<String> getMvelFilePaths() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
    
        // get subdirectories
        String path = "org/jbpm/task";
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
    
        HashSet<String> mvelFilePaths = new HashSet<String>();
        // get classes
        for (File directory : dirs) {
            List<String> mvelFiles = findMvelFiles(directory);
            if( ! mvelFiles.isEmpty() ) { 
                mvelFilePaths.addAll(mvelFiles);
            }
        }
    
        return mvelFilePaths;
    }

    /**
     * Recursive method used to find all MVEL files in a given directory.
     * 
     * @param directory The base directory
     * @return A list of relative paths to the MVEL files. 
     */
    private static List<String> findMvelFiles(File directory) {
        if (!directory.exists()) {
            return Collections.emptyList();
        }
        
        List<String> classes = new ArrayList<String>();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findMvelFiles(file));
            } else if (file.getName().matches(".*(?i:mvel)") ) { 
                String path = file.getPath();
                classes.add(path.substring(path.indexOf("org/jbpm/task")));
            }
        }
        return classes;
    }

}
