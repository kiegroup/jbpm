package org.jbpm.integration.console;

import static org.jbpm.integration.console.StatefulKnowledgeSessionUtil.checkPackagesFromGuvnor;
import static org.jbpm.integration.console.StatefulKnowledgeSessionUtil.getKnownPackages;
import static org.jbpm.integration.console.StatefulKnowledgeSessionUtil.setKagent;
import static org.jbpm.integration.console.StatefulKnowledgeSessionUtil.setKnownPackages;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.agent.KnowledgeAgent;
import org.drools.io.Resource;
import org.jbpm.integration.console.shared.GuvnorConnectionUtils;
import org.jbpm.integration.console.shared.model.GuvnorPackage;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

public class StatefulKnowledgeSessionUtilMockTest extends GuvnorConnectionUtils {

    @Test
    public void deletedAndRecreatedPackagesTest() {
        String existingPkg = "existingPkg";
        String existingPkgUuid = "existingPkg-uuid";
        String recreatedPkg = "recreatedPkg";
        String recreatedPkgUuid = "recreatedPkg-uuid"; 
        String archDeletedPkgUuid = "originalPkg-uuid"; 
        String deletedPkg = "deletedPkg";
        String deletedPkgUuid = "deletedPkg-uuid";
        String newPkg = "newBuildPackage";
        String newPkgUuid = "newPkg-uuid";
        
        // setup
        KnowledgeAgent mockKagent = mock(KnowledgeAgent.class);
        doNothing().when(mockKagent).applyChangeSet(any(Resource.class));
        setKagent(mockKagent);

        GuvnorConnectionUtils mockGuvnorUtils = mock(GuvnorConnectionUtils.class);
        doReturn(true).when(mockGuvnorUtils).guvnorExists();
        when(mockGuvnorUtils.getBuiltPackages()).thenCallRealMethod();
        when(mockGuvnorUtils.filterPackagesByUserDefinedList(anyList())).thenCallRealMethod();
        when(mockGuvnorUtils.isEmpty(anyString())).thenCallRealMethod();
        doReturn(true).when(mockGuvnorUtils).canBuildPackage(anyString());
     
        // information about the new packages
        List<GuvnorPackage> builtPkgs = new ArrayList<GuvnorPackage>();
        builtPkgs.add(new GuvnorPackage(existingPkg, false, existingPkgUuid));
        builtPkgs.add(new GuvnorPackage(recreatedPkg, false, recreatedPkgUuid));
        builtPkgs.add(new GuvnorPackage(newPkg, false, newPkgUuid));
        doReturn(builtPkgs).when(mockGuvnorUtils).getPackagesFromGuvnor();

        // information about the previous packages
        Map<String, String> knownPackages = new HashMap<String, String>();
        knownPackages.put(existingPkg, existingPkgUuid);
        knownPackages.put(recreatedPkg, archDeletedPkgUuid);
        knownPackages.put(deletedPkg, deletedPkgUuid);
        setKnownPackages(knownPackages);

        // test
        try {        
            checkPackagesFromGuvnor(mockGuvnorUtils);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Mocking did not succeed: " + e.getMessage());
        }

        verify(mockGuvnorUtils, times(1)).getPackagesFromGuvnor();
        List<String> updatePkgs = new ArrayList<String>();
        updatePkgs.add(newPkg);
        updatePkgs.add(recreatedPkg);
        ObjectEqualityArgumentMatcher<List<String>> matchesUpdatedPkgs = 
                new ObjectEqualityArgumentMatcher<List<String>>(updatePkgs);
        verify(mockGuvnorUtils, times(1)).createChangeSet(argThat(matchesUpdatedPkgs));
        
        Map<String, String> modifiedKnownPackages = getKnownPackages();
        assertNotNull("Existing package not kept in known packages [" + existingPkg + "]", modifiedKnownPackages.get(existingPkg));
        assertEquals("Existing package UUID changed? [" + existingPkg + "]", existingPkgUuid, modifiedKnownPackages.get(existingPkg));
        assertEquals("Recreated package uuid has not been updated [" + recreatedPkg + "]", recreatedPkgUuid, modifiedKnownPackages.get(recreatedPkg));
        assertNull("Deleted package still in known packages! [" + deletedPkg + "]", modifiedKnownPackages.get(deletedPkg));
        assertEquals("New package has not been added to known packages! [" + newPkg + "]", newPkgUuid, modifiedKnownPackages.get(newPkg));
    }

    /**
     * Matcher for particular arguments
     * 
     * @param <T> The object that the argument should match
     */
    private class ObjectEqualityArgumentMatcher<T> extends ArgumentMatcher<T> {
        
        T control;

        public ObjectEqualityArgumentMatcher(T thisObject) {
            this.control = thisObject;
        }

        @Override
        public boolean matches(Object argument) {
            if( argument instanceof List ) { 
               return Arrays.equals(
                       ((List) control).toArray(), 
                       ((List) argument).toArray());
            } else { 
                return control.equals(argument);
            }
        }
    }
}
