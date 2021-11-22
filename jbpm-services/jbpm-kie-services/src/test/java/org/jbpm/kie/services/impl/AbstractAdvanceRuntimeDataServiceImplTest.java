/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.kie.services.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.junit.Test;
import org.kie.api.runtime.query.QueryContext;

public class AbstractAdvanceRuntimeDataServiceImplTest {

    @Test
    public void testTaskComparator() {

        List<UserTaskInstanceWithPotOwnerDesc> tasks = Arrays.asList(
                                                                     new TestUserTaskInstance(12),
                                                                     new TestUserTaskInstance(23),
                                                                     new TestUserTaskInstance(43),
                                                                     new TestUserTaskInstance(98));
        List<Number> ids = Arrays.asList(98, 43, 9998, 12, 23, 9999);
        Collections.sort(tasks, new AbstractAdvanceRuntimeDataServiceImpl.TaskComparator(ids));
        assertEquals(Arrays.asList(98L, 43L, 12L, 23L), tasks.stream().map(UserTaskInstanceWithPotOwnerDesc::getTaskId).collect(Collectors.toList()));
    }
    
    @Test
    public void testProcessComparator() {

        List<ProcessInstanceWithVarsDesc> tasks = Arrays.asList(
                                                                     new TestProcessInstance(9999),
                                                                     new TestProcessInstance(23),
                                                                     new TestProcessInstance(9998),
                                                                     new TestProcessInstance(98));
        List<Number> ids = Arrays.asList(98, 43, 9998, 12, 23, 9999);
        Collections.sort(tasks, new AbstractAdvanceRuntimeDataServiceImpl.ProcessComparator(ids));
        assertEquals(Arrays.asList(98L, 9998L, 23L, 9999L), tasks.stream().map(ProcessInstanceWithVarsDesc::getId).collect(Collectors.toList()));
        
    }
    
    @Test
    public void testSelectFields() {
        assertEquals("SELECT DISTINCT pil.processInstanceId", AbstractAdvanceRuntimeDataServiceImpl.getSelectFields("pil.processInstanceId", new QueryContext("pil.processInstanceId", true)));
        assertEquals("SELECT DISTINCT pil.processInstanceId", AbstractAdvanceRuntimeDataServiceImpl.getSelectFields("pil.processInstanceId", new QueryContext("processInstanceId", true)));
        assertEquals("SELECT DISTINCT pil.processInstanceId, taskId", AbstractAdvanceRuntimeDataServiceImpl.getSelectFields("pil.processInstanceId", new QueryContext("taskId", true)));
        assertEquals("SELECT DISTINCT pil.processInstanceId, taskId", AbstractAdvanceRuntimeDataServiceImpl.getSelectFields("pil.processInstanceId", new QueryContext("   taskId", true)));
    }
    

    private static class TestProcessInstance implements ProcessInstanceWithVarsDesc {
        
        private long id;
        
        public TestProcessInstance(long id) {
            this.id = id;
        }

        @Override
        public String getProcessId() {
            return null;
        }

        @Override
        public Long getId() {
       
            return id;
        }

        @Override
        public String getProcessName() {
       
            return null;
        }

        @Override
        public Integer getState() {
       
            return null;
        }

        @Override
        public String getDeploymentId() {
       
            return null;
        }

        @Override
        public Date getDataTimeStamp() {
       
            return null;
        }

        @Override
        public String getProcessVersion() {
       
            return null;
        }

        @Override
        public String getInitiator() {
       
            return null;
        }

        @Override
        public String getProcessInstanceDescription() {
       
            return null;
        }

        @Override
        public String getCorrelationKey() {
       
            return null;
        }

        @Override
        public Long getParentId() {
       
            return null;
        }

        @Override
        public Date getSlaDueDate() {
       
            return null;
        }

        @Override
        public Integer getSlaCompliance() {
       
            return null;
        }

        @Override
        public List<org.jbpm.services.api.model.UserTaskInstanceDesc> getActiveTasks() {
       
            return null;
        }

        @Override
        public Map<String, Object> getVariables() {
       
            return null;
        }

        @Override
        public Map<String, Object> getExtraData() {
       
            return null;
        }

    }



    
    private static class TestUserTaskInstance implements UserTaskInstanceWithPotOwnerDesc {

        public TestUserTaskInstance(long id) {
            this.id = id;
        }

        private long id;

        @Override
        public Long getTaskId() {

            return id;
        }

        @Override
        public String getStatus() {

            return null;
        }

        @Override
        public Date getActivationTime() {

            return null;
        }

        @Override
        public String getName() {

            return null;
        }

        @Override
        public String getDescription() {

            return null;
        }

        @Override
        public Integer getPriority() {

            return null;
        }

        @Override
        public String getCreatedBy() {

            return null;
        }

        @Override
        public Date getCreatedOn() {

            return null;
        }

        @Override
        public Date getDueDate() {

            return null;
        }

        @Override
        public Long getProcessInstanceId() {

            return null;
        }

        @Override
        public String getProcessId() {

            return null;
        }

        @Override
        public String getActualOwner() {

            return null;
        }

        @Override
        public String getDeploymentId() {

            return null;
        }

        @Override
        public String getFormName() {

            return null;
        }

        @Override
        public Long getWorkItemId() {

            return null;
        }

        @Override
        public Integer getSlaCompliance() {

            return null;
        }

        @Override
        public Date getSlaDueDate() {

            return null;
        }

        @Override
        public void setSlaCompliance(Integer slaCompliance) {

        }

        @Override
        public void setSlaDueDate(Date slaDueDate) {

        }

        @Override
        public void setSubject(String subject) {

        }

        @Override
        public void setCorrelationKey(String correlationKey) {

        }

        @Override
        public Integer getProcessType() {

            return null;
        }

        @Override
        public void setProcessType(Integer processType) {

        }

        @Override
        public List<String> getPotentialOwners() {

            return null;
        }

        @Override
        public String getCorrelationKey() {

            return null;
        }

        @Override
        public Date getLastModificationDate() {

            return null;
        }

        @Override
        public String getLastModificationUser() {

            return null;
        }

        @Override
        public String getSubject() {

            return null;
        }

        @Override
        public Map<String, Object> getInputdata() {

            return null;
        }

        @Override
        public Map<String, Object> getOutputdata() {

            return null;
        }

        @Override
        public String getProcessInstanceDescription() {

            return null;
        }

        @Override
        public Map<String, Object> getProcessVariables() {

            return null;
        }

        @Override
        public Map<String, Object> getExtraData() {

            return null;
        }
    }

}
