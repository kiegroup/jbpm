/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.audit;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.drools.WorkingMemory;
import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.RuleFlowLogEvent;
import org.drools.audit.event.RuleFlowNodeLogEvent;
import org.drools.audit.event.RuleFlowVariableLogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeRuntime;
import org.jbpm.process.audit.event.ExtendedRuleFlowLogEvent;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;

/**
 * Enables history log via JPA.
 * 
 */
public class JPAWorkingMemoryDbLogger extends WorkingMemoryLogger {

    protected Environment env;

    public JPAWorkingMemoryDbLogger(WorkingMemory workingMemory) {
        super(workingMemory);
        env = workingMemory.getEnvironment();
    }
    
    public JPAWorkingMemoryDbLogger(KnowledgeRuntimeEventManager session) {
    	super(session);
        if (session instanceof KnowledgeRuntime) {
            env = ((KnowledgeRuntime) session).getEnvironment();
        } else if (session instanceof StatelessKnowledgeSessionImpl) {
        	env = ((StatelessKnowledgeSessionImpl) session).getEnvironment();
        } else {
            throw new IllegalArgumentException(
                "Not supported session in logger: " + session.getClass());
        }
    }

    public void logEventCreated(LogEvent logEvent) {
        switch (logEvent.getType()) {
            case LogEvent.BEFORE_RULEFLOW_CREATED:
                RuleFlowLogEvent processEvent = (RuleFlowLogEvent) logEvent;
                addProcessLog(processEvent);
                break;
            case LogEvent.AFTER_RULEFLOW_COMPLETED:
            	processEvent = (RuleFlowLogEvent) logEvent;
                updateProcessLog(processEvent);
                break;
            case LogEvent.BEFORE_RULEFLOW_NODE_TRIGGERED:
            	RuleFlowNodeLogEvent nodeEvent = (RuleFlowNodeLogEvent) logEvent;
            	addNodeEnterLog(nodeEvent.getProcessInstanceId(), nodeEvent.getProcessId(), nodeEvent.getNodeInstanceId(), nodeEvent.getNodeId(), nodeEvent.getNodeName());
                break;
            case LogEvent.BEFORE_RULEFLOW_NODE_EXITED:
            	nodeEvent = (RuleFlowNodeLogEvent) logEvent;
            	addNodeExitLog(nodeEvent.getProcessInstanceId(), nodeEvent.getProcessId(), nodeEvent.getNodeInstanceId(), nodeEvent.getNodeId(), nodeEvent.getNodeName());
                break;
            case LogEvent.AFTER_VARIABLE_INSTANCE_CHANGED:
            	RuleFlowVariableLogEvent variableEvent = (RuleFlowVariableLogEvent) logEvent;
            	addVariableLog(variableEvent.getProcessInstanceId(), variableEvent.getProcessId(), variableEvent.getVariableInstanceId(), variableEvent.getVariableId(), variableEvent.getObjectToString());
                break;
            default:
                // ignore all other events
        }
    }

    private void addProcessLog(RuleFlowLogEvent processEvent) {
        ProcessInstanceLog log = new ProcessInstanceLog(processEvent.getProcessInstanceId(), processEvent.getProcessId());
        if (processEvent instanceof ExtendedRuleFlowLogEvent) {
            log.setParentProcessInstanceId(((ExtendedRuleFlowLogEvent) processEvent).getParentProcessInstanceId());
        }
        getEntityManager().persist(log);
    }

    @SuppressWarnings("unchecked")
    private void updateProcessLog(RuleFlowLogEvent processEvent) {
        List<ProcessInstanceLog> result = getEntityManager().createQuery(
            "from ProcessInstanceLog as log where log.processInstanceId = ? and log.end is null")
                .setParameter(1, processEvent.getProcessInstanceId()).getResultList();
        if (result != null && result.size() != 0) {
            ProcessInstanceLog log = result.get(result.size() - 1);
            log.setEnd(new Date());
            if (processEvent instanceof ExtendedRuleFlowLogEvent) {
                log.setStatus(((ExtendedRuleFlowLogEvent) processEvent).getProcessInstanceState());
                log.setOutcome(((ExtendedRuleFlowLogEvent) processEvent).getOutcome());
            }
            getEntityManager().merge(log);
        }
    }

    private void addNodeEnterLog(long processInstanceId, String processId, String nodeInstanceId, String nodeId, String nodeName) {
        NodeInstanceLog log = new NodeInstanceLog(
    		NodeInstanceLog.TYPE_ENTER, processInstanceId, processId, nodeInstanceId, nodeId, nodeName);
        getEntityManager().persist(log);
    }

    private void addNodeExitLog(long processInstanceId,
            String processId, String nodeInstanceId, String nodeId, String nodeName) {
        NodeInstanceLog log = new NodeInstanceLog(
            NodeInstanceLog.TYPE_EXIT, processInstanceId, processId, nodeInstanceId, nodeId, nodeName);
        getEntityManager().persist(log);
    }

    private void addVariableLog(long processInstanceId, String processId, String variableInstanceId, String variableId, String objectToString) {
    	VariableInstanceLog log = new VariableInstanceLog(
    		processInstanceId, processId, variableInstanceId, variableId, objectToString);
        getEntityManager().persist(log);
    }

    public void dispose() {
    }

    protected EntityManager getEntityManager() {
        return (EntityManager) env.get(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER);
    }

    public void beforeProcessStarted(ProcessStartedEvent event) {
        long parentProcessInstanceId = -1;
        try {
            ProcessInstanceImpl processInstance = (ProcessInstanceImpl) event.getProcessInstance();
            parentProcessInstanceId = (Long) processInstance.getMetaData().get("ParentProcessInstanceId");
        } catch (Exception e) {
            //in case of problems with getting hold of parentProcessInstanceId don't break the operation
        }
        LogEvent logEvent =  new ExtendedRuleFlowLogEvent( LogEvent.BEFORE_RULEFLOW_CREATED,
                event.getProcessInstance().getProcessId(),
                event.getProcessInstance().getProcessName(),
                event.getProcessInstance().getId(), parentProcessInstanceId) ;
        
        // filters are not available from super class, TODO make fireLogEvent protected instead of private in WorkinMemoryLogger
        logEventCreated( logEvent );
    }

    public void afterProcessCompleted(ProcessCompletedEvent event) {
        String outcome = null;
        try {
            ProcessInstanceImpl processInstance = (ProcessInstanceImpl) event.getProcessInstance();
            outcome = processInstance.getOutcome();
        } catch (Exception e) {
            //in case of problems with getting hold of parentProcessInstanceId don't break the operation
        }
        LogEvent logEvent =  new ExtendedRuleFlowLogEvent(LogEvent.AFTER_RULEFLOW_COMPLETED,
                event.getProcessInstance().getProcessId(),
                event.getProcessInstance().getProcessName(),
                event.getProcessInstance().getId(), event.getProcessInstance().getState(), outcome) ;
        
        // filters are not available from super class, TODO make fireLogEvent protected instead of private in WorkinMemoryLogger
        logEventCreated( logEvent );
    }

}
