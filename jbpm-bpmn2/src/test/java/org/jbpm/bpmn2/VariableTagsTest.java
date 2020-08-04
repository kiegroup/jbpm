/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package org.jbpm.bpmn2;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.core.context.variable.VariableViolationException;
import org.jbpm.process.instance.event.listeners.VariableGuardProcessEventListener;
import org.jbpm.test.util.TestIdentityProvider;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
 public class VariableTagsTest extends JbpmBpmn2TestCase {

     @Parameters
     public static Collection<Object[]> persistence() {
         Object[][] data = new Object[][] { { false }, { true } };
         return Arrays.asList(data);
     };

     private KieSession ksession;
     private TestWorkItemHandler workItemHandler;

     public VariableTagsTest(boolean persistence) throws Exception {
         super(persistence);
     }

     @BeforeClass
     public static void setup() throws Exception {
         setUpDataSource();
     }

     @After
     public void dispose() {
         if (ksession != null) {
             abortProcessInstances(ksession);
             ksession.dispose();
             ksession = null;
         }
     }

     @Test
     public void testProcessWithMissingRequiredVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-required-variable-tags.bpmn2");

         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.startProcess("approvals"));
     }
     
     @Test
     public void testProcessWithNullRequiredVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-required-variable-tags.bpmn2");

         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", null);
         
         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.startProcess("approvals", parameters));
     }
     
     @Test
     public void testProcessWithEmptyRequiredVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-required-variable-tags.bpmn2");

         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "   ");
         
         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.startProcess("approvals", parameters));
     }


     @Test
     public void testProcessWithRequiredVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-required-variable-tags.bpmn2");

         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");

         ProcessInstance processInstance = ksession.startProcess("approvals", parameters);
         assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
         ksession = restoreSession(ksession, true);
         ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
         WorkItem workItem = workItemHandler.getWorkItem();
         assertNotNull(workItem);
         ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

         workItem = workItemHandler.getWorkItem();
         assertNotNull(workItem);        
         ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

         assertProcessInstanceFinished(processInstance, ksession);
     }

     @Test
     public void testProcessWithReadonlyVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-readonly-variable-tags.bpmn2");
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");

         ProcessInstance processInstance = ksession.startProcess("approvals", parameters);
         assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);        
         WorkItem workItem = workItemHandler.getWorkItem();
         assertNotNull(workItem);

         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.getWorkItemManager().completeWorkItem(workItem.getId(), Collections.singletonMap("ActorId", "john2")));
         ksession.abortProcessInstance(processInstance.getId());

         assertProcessInstanceFinished(processInstance, ksession);
     }
     
     @Test
     public void testProcessWithNullReadonlyVariable() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-readonly-variable-tags.bpmn2");
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", null);

         startAndCompleteProcess(parameters);
     }
     
     @Test
     public void testProcessWithCustomVariableTag() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-custom-variable-tags.bpmn2");
         
         ksession.addEventListener(new DefaultProcessEventListener() {

             @Override
             public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                 if (event.hasTag("onlyAdmin")) {
                     throw new VariableViolationException(event.getProcessInstance().getId(), event.getVariableId(), "Variable can only be set by admins");
                 }
             }
             
         });
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");
         
         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.startProcess("approvals", parameters));
     }
     
     @Test
     public void testProcessWithRestrictedVariableTagNoRequiredRole() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-restricted-variable-tags.bpmn2");
         
         ksession.addEventListener(new VariableGuardProcessEventListener("AdminRole", new TestIdentityProvider(Arrays.asList("NormalRole"))) );
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");
         
         assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> ksession.startProcess("approvals", parameters));
     }
     
     @Test
     public void testProcessWithRestrictedVariableTagRequiredRole() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-restricted-variable-tags.bpmn2");
         
         ksession.addEventListener(new VariableGuardProcessEventListener("AdminRole", new TestIdentityProvider(Arrays.asList("AdminRole"))) );
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");
         
         startAndCompleteProcess(parameters);
     }
     
     @Test
     public void testProcessWithCustomVariableTagInsteadOfRestrictedTag() throws Exception {
         ksession = createSessionAndRegisterWorkItemHandler("variable-tags/approval-with-custom-variable-tags.bpmn2");
         
         ksession.addEventListener(new VariableGuardProcessEventListener("AdminRole", new TestIdentityProvider(Arrays.asList("NormalRole"))) );
         
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("approver", "john");
         
         // With NormalRole, process is completed because event has no "restricted" tag but custom ("onlyAdmin")
         startAndCompleteProcess(parameters);
     }
     
     private KieSession createSessionAndRegisterWorkItemHandler(String process) throws Exception {
         KieBase kbase = createKnowledgeBase(process);
         KieSession ksession = createKnowledgeSession(kbase);
         workItemHandler = new TestWorkItemHandler();
         ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
         return ksession;
     }
     
     private void startAndCompleteProcess(Map<String, Object> parameters) {
         ProcessInstance processInstance = ksession.startProcess("approvals", parameters);
         assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);        
         completeWorkItem();
         completeWorkItem();
         assertProcessInstanceFinished(processInstance, ksession);
     }

     private void completeWorkItem() {
         WorkItem workItem = workItemHandler.getWorkItem();
         assertNotNull(workItem);
         ksession.getWorkItemManager().completeWorkItem(workItem.getId(), Collections.singletonMap("ActorId", "john"));
     }
 }