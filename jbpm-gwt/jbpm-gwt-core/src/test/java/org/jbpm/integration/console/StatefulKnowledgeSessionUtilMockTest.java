package org.jbpm.integration.console;

import static org.junit.Assert.*;
import static org.jbpm.integration.console.StatefulKnowledgeSessionUtil.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.agent.KnowledgeAgent;
import org.drools.io.Resource;
import org.jbpm.integration.console.shared.GuvnorConnectionUtils;
import org.junit.Test;

public class StatefulKnowledgeSessionUtilMockTest {

    @Test
    public void deletedPackagesTest() { 
        String existingPackage = "existingPackage";
        
        // setup
        KnowledgeAgent mockKagent = mock(KnowledgeAgent.class);
        doNothing().when(mockKagent).applyChangeSet(any(Resource.class));
        setKagent(mockKagent);
        
        GuvnorConnectionUtils mockGuvnorUtils = mock(GuvnorConnectionUtils.class);
        doReturn(true).when(mockGuvnorUtils).guvnorExists();
        List<String> builtPkgNames = new ArrayList<String>();
        builtPkgNames.add("newBuiltPackage");
        builtPkgNames.add(existingPackage);
        doReturn(builtPkgNames).when(mockGuvnorUtils).getBuiltPackageNames();
        
        Set<String> knownPackages = new HashSet<String>();
        knownPackages.add(existingPackage);
        knownPackages.add("deletedPackage");
        setKnownPackages(knownPackages);
       
        // test
        try { 
            checkPackagesFromGuvnor(mockGuvnorUtils);
        } catch( Exception e ) { 
           assertTrue( e instanceof IllegalArgumentException ); 
        }
       
        Set<String> modifiedKnownPackages = getKnownPackages();
        assertTrue( "Existing package not kept in known packages", modifiedKnownPackages.contains(existingPackage));
    }
    
    
}
