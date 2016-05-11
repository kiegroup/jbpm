package org.jbpm.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManager;
import org.jbpm.test.workitem.ExceptionWorkItemHandler;
import org.jbpm.test.workitem.NoExceptionWorkItemHandler;
import org.jbpm.workflow.instance.WorkflowRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;


public class RetryWorkItemTest  extends JbpmJUnitBaseTestCase {
    private static final String RETRY_WORKITEM_PROCESS_ID = "org.jbpm.test.retryWorkitem";
    
    @Before
    public void setup(){
        
    }
    
    @Test
    public void workItemRecoveryTest() {

        // setup
        RuntimeManager runtimeManager = createRuntimeManager("retry-workitem.bpmn2");
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession kieSession = runtimeEngine.getKieSession();
        WorkItemManager workItemManager = (org.drools.core.process.instance.WorkItemManager) kieSession.getWorkItemManager();
        workItemManager.registerWorkItemHandler("ExceptionWorkitem", new ExceptionWorkItemHandler());
        workItemManager.registerWorkItemHandler("NoExceptionWorkitem", new NoExceptionWorkItemHandler());
        
        
        org.kie.api.runtime.process.WorkflowProcessInstance p =null;
        Collection<NodeInstance> nis =null;
        try {
            ProcessInstance processIstance = kieSession.startProcess(RETRY_WORKITEM_PROCESS_ID);
            org.junit.Assert.assertEquals( processIstance.getState(),1 );
            
        } catch (WorkflowRuntimeException wre) {
            p =(org.kie.api.runtime.process.WorkflowProcessInstance) kieSession.getProcessInstance(wre.getProcessInstanceId());
            nis = ((org.kie.api.runtime.process.WorkflowProcessInstance)p).getNodeInstances();
            
        }
      for(NodeInstance di : nis){
          org.jbpm.workflow.instance.node.WorkItemNodeInstance in = (org.jbpm.workflow.instance.node.WorkItemNodeInstance)di;
          in.getMetaData();
          org.junit.Assert.assertEquals( in.getWorkItem().getState(),0 );
          Map<String,Object> map = new HashMap<String,Object>();
          map.put( "name", "xiabai" );
          retryWorkItem(workItemManager,di.getId(),map);
      }
      
        org.junit.Assert.assertEquals( p.getState(),2);
        runtimeManager.disposeRuntimeEngine(runtimeEngine);
        runtimeManager.close();
    }
    
    private void retryWorkItem(WorkItemManager workItemManager,Long workItemId,Map<String,Object> map){
        
        if ( workItemManager instanceof DefaultWorkItemManager){
            ((DefaultWorkItemManager)workItemManager).retryWorkItemWithParams( workItemId, map );
        }
        if ( workItemManager instanceof JPAWorkItemManager){
            ((JPAWorkItemManager)workItemManager).retryWorkItemWithParams( workItemId, map );
        }
    }
}
