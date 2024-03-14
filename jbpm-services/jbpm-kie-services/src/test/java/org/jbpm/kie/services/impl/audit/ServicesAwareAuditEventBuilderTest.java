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
package org.jbpm.kie.services.impl.audit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.event.ProcessStartedEventImpl;
import org.drools.core.event.rule.impl.ProcessDataChangedEventImpl;
import org.jbpm.kie.services.test.ProcessServiceImplTest;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.workflow.core.WorkflowProcess;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessDataChangedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.internal.process.CorrelationKey;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ServicesAwareAuditEventBuilderTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessServiceImplTest.class);
    
    private static final Date TEST_DATE = Date.from(LocalDate.of(2024, 3, 14).atStartOfDay(ZoneId.systemDefault()).toInstant());
    
    private static final String TEST_DEPLOYMENT_UNIT = "unit1";

    private Map<String, Object> processMetadata = new HashMap<>();

    private ServicesAwareAuditEventBuilder builder;

    @Mock
    ProcessInstanceImpl processInstance;

    @Mock
    KieSession kieRuntime;

    @Mock
    WorkflowProcess process;

    @Mock
    CorrelationKey correlationKey;

    @Mock
    VariableScopeInstance variableScope;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        processMetadata.put("CorrelationKey", correlationKey);

        builder = new ServicesAwareAuditEventBuilder();
        builder.setIdentityProvider(identityProvider);
        builder.setDeploymentUnitId(TEST_DEPLOYMENT_UNIT);

        setUpMocks();
    }

    private void setUpMocks() {
        when(kieRuntime.getIdentifier()).thenReturn(2L);

        when(processInstance.getId()).thenReturn(1L);
        when(processInstance.getDescription()).thenReturn("Some test Process");
        when(processInstance.getSlaCompliance()).thenReturn(0);
        
        when(processInstance.getSlaDueDate()).thenReturn(TEST_DATE);
        when(processInstance.getMetaData()).thenReturn(processMetadata);

        when(processInstance.getProcess()).thenReturn(process);
        when(process.getProcessType()).thenReturn(WorkflowProcess.PROCESS_TYPE);
        when(process.getName()).thenReturn("test-process");
        when(process.getVersion()).thenReturn(VERSION);

        when(processInstance.getContextInstance(eq(VariableScope.VARIABLE_SCOPE))).thenReturn(variableScope);
        when(variableScope.getVariables()).thenReturn(processMetadata);

        when(correlationKey.toExternalForm()).thenReturn("1");
    }

    /**
     * Test build the ProcessInstanceLog for a regular process start
     */
    @Test
    public void testBuildProcessStartedEvent() {

        ProcessStartedEvent pse = new ProcessStartedEventImpl(processInstance, kieRuntime);

        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(pse);

        assertEquals("testUser", log.getIdentity());
    }
    
    /**
     * Test build the ProcessInstanceLog for data change
     */
    @Test
    public void testBuildProcessDataChangedEventImpl() {

    	ProcessDataChangedEvent pdce = new ProcessDataChangedEventImpl(processInstance, kieRuntime);

        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(pdce);

        assertEquals(TEST_DATE, log.getSlaDueDate());
        
        assertEquals(TEST_DEPLOYMENT_UNIT, log.getExternalId());
       
    }

    /**
     * Test build the ProcessInstanceLog for a process with initiator metadata
     * and user auth bypass not enabled
     */
    @Test
    public void testBuildProcessStartedEventWithInitiatorAndNoUserAuthBypass() {

        processMetadata.put("initiator", "john");
        ProcessStartedEvent pse = new ProcessStartedEventImpl(processInstance, kieRuntime);

        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(pse);

        assertEquals("testUser", log.getIdentity());
    }

    /**
     * Test build the ProcessInstanceLog for a process with initiator metadata
     * and user auth bypass enabled
     */
    @Test
    public void testBuildProcessStartedEventWithInitiatorAndUserAuthBypassEnabled() {

        processMetadata.put("initiator", "john");

        enableSetInitiator(builder);

        ProcessStartedEvent pse = new ProcessStartedEventImpl(processInstance, kieRuntime);
        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(pse);

        assertEquals("john", log.getIdentity());
    }

    /**
     * Test build the ProcessInstanceLog for a process with user auth bypass
     * enabled but no initiator set on process data
     */
    @Test
    public void testBuildProcessStartedEventWithUserAuthBypassEnabledButNoInitiator() {

        enableSetInitiator(builder);

        ProcessStartedEvent pse = new ProcessStartedEventImpl(processInstance, kieRuntime);

        ProcessInstanceLog log = (ProcessInstanceLog) builder.buildEvent(pse);

        assertEquals("testUser", log.getIdentity());
    }

    /**
     * Utilitary method to config builder to allow initiator to be collected
     * from process data
     *
     * @param builder
     */
    private void enableSetInitiator(ServicesAwareAuditEventBuilder builder) {
        try {
            Field allowSetInitiatorField = builder.getClass().getDeclaredField("allowSetInitiator");
            allowSetInitiatorField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(allowSetInitiatorField, allowSetInitiatorField.getModifiers() & ~Modifier.FINAL);
            allowSetInitiatorField.set(builder, Boolean.TRUE);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            // Tests will misbehave
            throw new RuntimeException(ex);
        }
    }
}
