package org.jbpm.integration.console.shared;

import static org.junit.Assert.*;
import java.io.InputStream;
import java.util.List;

import org.jbpm.integration.console.shared.model.GuvnorPackage;
import org.junit.Test;

public class GuvnorConnectionUtilsTest {

    @Test
    public void packageXmlTest() throws Exception {
        String fileLoc = "/packages.xml";
        InputStream inputStream = this.getClass().getResourceAsStream(fileLoc);
       
        List<GuvnorPackage> pkgs = GuvnorConnectionUtils.getPackagesFromXmlInputStream(inputStream);
        assertEquals( "Number of packages", 2, pkgs.size() );
      
        for( GuvnorPackage pkg : pkgs ) { 
            assertNotNull(pkg.getArchived());
            assertNotNull(pkg.getUuid());
            assertNotNull(pkg.getTitle());
        }
    }
}
