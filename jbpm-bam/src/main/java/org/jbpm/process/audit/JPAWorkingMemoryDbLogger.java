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

import org.drools.WorkingMemory;
import org.drools.audit.WorkingMemoryLogger;
import org.drools.audit.event.LogEvent;
import org.drools.audit.event.RuleFlowLogEvent;
import org.drools.audit.event.RuleFlowNodeLogEvent;
import org.drools.audit.event.RuleFlowVariableLogEvent;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.runtime.Environment;
import org.drools.runtime.KnowledgeRuntime;
import org.jbpm.process.audit.persistence.BAMPersistenceManager;
import org.jbpm.process.audit.persistence.BAMPersistenceManagerFactory;

/**
 * Enables history log via JPA.
 * 
 */
public class JPAWorkingMemoryDbLogger extends WorkingMemoryLogger {

    private BAMPersistenceManager manager;

    public JPAWorkingMemoryDbLogger(WorkingMemory workingMemory) {
        super(workingMemory);
        manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(workingMemory.getEnvironment());
    }
    
    public JPAWorkingMemoryDbLogger(KnowledgeRuntimeEventManager session) {
    	super(session);
    	Environment env = null;
    	if (session instanceof KnowledgeRuntime) {
            env = ((KnowledgeRuntime) session).getEnvironment();
        } else if (session instanceof StatelessKnowledgeSessionImpl) {
        	env = ((StatelessKnowledgeSessionImpl) session).getEnvironment();
        } else {
            throw new IllegalArgumentException(
                "Not supported session in logger: " + session.getClass());
        }
        manager = BAMPersistenceManagerFactory.getBAMPersistenceManager(env);
    }

    public void logEventCreated(LogEvent logEvent) {
        boolean txOwner = manager.beginTransaction();
        try {
            switch (logEvent.getType()) {
                case LogEvent.BEFORE_RULEFLOW_CREATED:
                    RuleFlowLogEvent processEvent = (RuleFlowLogEvent) logEvent;
                    manager.addProcessLog(processEvent.getProcessInstanceId(), processEvent.getProcessId());
                    break;
                case LogEvent.AFTER_RULEFLOW_COMPLETED:
                	processEvent = (RuleFlowLogEvent) logEvent;
                	manager.updateProcessLog(processEvent.getProcessInstanceId());
                    break;
                case LogEvent.BEFORE_RULEFLOW_NODE_TRIGGERED:
                	RuleFlowNodeLogEvent nodeEvent = (RuleFlowNodeLogEvent) logEvent;
                	manager.addNodeEnterLog(nodeEvent.getProcessInstanceId(), nodeEvent.getProcessId(), nodeEvent.getNodeInstanceId(), nodeEvent.getNodeId(), nodeEvent.getNodeName());
                    break;
                case LogEvent.BEFORE_RULEFLOW_NODE_EXITED:
                	nodeEvent = (RuleFlowNodeLogEvent) logEvent;
                	manager.addNodeExitLog(nodeEvent.getProcessInstanceId(), nodeEvent.getProcessId(), nodeEvent.getNodeInstanceId(), nodeEvent.getNodeId(), nodeEvent.getNodeName());
                    break;
                case LogEvent.AFTER_VARIABLE_INSTANCE_CHANGED:
                	RuleFlowVariableLogEvent variableEvent = (RuleFlowVariableLogEvent) logEvent;
                	manager.addVariableLog(variableEvent.getProcessInstanceId(), variableEvent.getProcessId(), variableEvent.getVariableInstanceId(), variableEvent.getVariableId(), variableEvent.getObjectToString());
                    break;
                default:
                    // ignore all other events
            }
        } finally {
            manager.endTransaction(txOwner);
        }
    }

    public void dispose() {
    }

}
