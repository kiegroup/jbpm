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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.event.AuditEvent;
import org.jbpm.process.audit.event.DefaultAuditEventBuilderImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.kie.api.event.process.ProcessDataChangedEvent;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessAsyncNodeScheduledEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.internal.identity.IdentityProvider;

public class ServicesAwareAuditEventBuilder extends DefaultAuditEventBuilderImpl {

    private IdentityProvider identityProvider;

    private String deploymentUnitId;

    private final Boolean allowSetInitiator = Boolean.parseBoolean(System.getProperty("org.kie.server.bypass.auth.user", "false"));

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public void setIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @Override
    public AuditEvent buildEvent(ProcessStartedEvent pse) {

        ProcessInstanceLog log = (ProcessInstanceLog) super.buildEvent(pse);
        log.setIdentity(getIdentity(pse));
        log.setExternalId(deploymentUnitId);
        return log;
    }

    @Override
    public AuditEvent buildEvent(ProcessCompletedEvent pce, Object log) {
        ProcessInstanceLog instanceLog = (ProcessInstanceLog) super.buildEvent(pce, log);
        instanceLog.setExternalId(deploymentUnitId);
        return instanceLog;

    }

    @Override
    public AuditEvent buildEvent(ProcessNodeTriggeredEvent pnte) {
        NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnte);
        nodeInstanceLog.setExternalId(deploymentUnitId);
        return nodeInstanceLog;

    }

    @Override
    public AuditEvent buildEvent(ProcessAsyncNodeScheduledEvent pnte) {
        NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnte);
        nodeInstanceLog.setExternalId(deploymentUnitId);
        return nodeInstanceLog;

    }

    @Override
    public AuditEvent buildEvent(ProcessNodeLeftEvent pnle, Object log) {
        NodeInstanceLog nodeInstanceLog = (NodeInstanceLog) super.buildEvent(pnle, log);
        nodeInstanceLog.setExternalId(deploymentUnitId);
        return nodeInstanceLog;
    }

    @Override
    public AuditEvent buildEvent(ProcessVariableChangedEvent pvce) {
        VariableInstanceLog variableLog = (VariableInstanceLog) super.buildEvent(pvce);
        variableLog.setExternalId(deploymentUnitId);
        return variableLog;
    }

    @Override
    public AuditEvent buildEvent(ProcessDataChangedEvent pdce) {
        ProcessInstanceLog instanceLog = (ProcessInstanceLog) super.buildEvent(pdce);
        instanceLog.setExternalId(deploymentUnitId);
        return instanceLog;
    }

    public String getDeploymentUnitId() {
        return deploymentUnitId;
    }

    public void setDeploymentUnitId(String deploymentUnitId) {
        this.deploymentUnitId = deploymentUnitId;
    }

    /**
     * Utilitary method to get the identity to save on ProcessInstanceLog. It
     * checks if bypass user authentication is set and if set, checks if the
     * value of initiator in process variables is set and uses it. Otherwise
     * will use the value from the identity provider.
     *
     * @param ProcessStartedEvent pse
     *
     * @return String the identity to be used as process starter
     */
    private String getIdentity(ProcessStartedEvent pse) {
        String identity = identityProvider.getName();
        if (allowSetInitiator) {
            ProcessInstance pi = (ProcessInstance) pse.getProcessInstance();
            VariableScopeInstance variableScope = (VariableScopeInstance) pi.getContextInstance(VariableScope.VARIABLE_SCOPE);
            Map<String, Object> processVariables = variableScope.getVariables();
            String initiator = (String) processVariables.get("initiator");

            identity = !StringUtils.isEmpty(initiator) ? initiator : identity;
        }
        return identity;
    }
}
