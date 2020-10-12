/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.casemgmt.impl.event;

import java.util.List;
import java.util.Map;

import org.drools.core.event.AbstractEventSupport;
import org.jbpm.casemgmt.api.event.CaseCancelEvent;
import org.jbpm.casemgmt.api.event.CaseCloseEvent;
import org.jbpm.casemgmt.api.event.CaseCommentEvent;
import org.jbpm.casemgmt.api.event.CaseDataEvent;
import org.jbpm.casemgmt.api.event.CaseDestroyEvent;
import org.jbpm.casemgmt.api.event.CaseDynamicSubprocessEvent;
import org.jbpm.casemgmt.api.event.CaseDynamicTaskEvent;
import org.jbpm.casemgmt.api.event.CaseEventListener;
import org.jbpm.casemgmt.api.event.CaseReopenEvent;
import org.jbpm.casemgmt.api.event.CaseRoleAssignmentEvent;
import org.jbpm.casemgmt.api.event.CaseStartEvent;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CommentInstance;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.identity.IdentityProvider;

/**
 * Responsible for firing case related events to notify registered CaseEventListeners 
 *
 */
public class CaseEventSupport extends AbstractEventSupport<CaseEventListener> {

    private IdentityProvider identityProvider;
    
    public CaseEventSupport(IdentityProvider identityProvider, List<CaseEventListener> caseEventListeners) {
        this.identityProvider = identityProvider;
        if (caseEventListeners != null) {
            caseEventListeners.forEach( cvl -> addEventListener(cvl));
        }
    }
    
    /*
     * fire*CaseStarted
     */
    
    public void fireBeforeCaseStarted(String caseId, String deploymentId, String caseDefinitionId, CaseFileInstance caseFile) {
        if ( hasListeners() ) {
            final CaseStartEvent event = new CaseStartEvent(identityProvider.getName(), caseId, deploymentId, caseDefinitionId, caseFile);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseStarted(e) );
        }
    }

    public void fireAfterCaseStarted(String caseId, String deploymentId, String caseDefinitionId, CaseFileInstance caseFile, long processInstanceId) {
        if ( hasListeners() ) {
            final CaseStartEvent event = new CaseStartEvent(identityProvider.getName(), caseId, deploymentId, caseDefinitionId, caseFile, processInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseStarted(e) );
        }
    }
    
    /*
     * fire*CaseClosed
     */
    public void fireBeforeCaseClosed(String caseId, CaseFileInstance caseFile, String comment) {
        if ( hasListeners() ) {
            final CaseCloseEvent event = new CaseCloseEvent(identityProvider.getName(), caseId, caseFile, comment);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseClosed(e) );
        }
    }

    public void fireAfterCaseClosed(String caseId, CaseFileInstance caseFile, String comment) {
        if ( hasListeners() ) {
            final CaseCloseEvent event = new CaseCloseEvent(identityProvider.getName(), caseId, caseFile, comment);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseClosed(e) );
        }
    }
    
    /*
     * fire*CaseCancelled
     */
    public void fireBeforeCaseCancelled(String caseId, CaseFileInstance caseFile, List<Long> processInstanceIds) {
        if ( hasListeners() ) {
            final CaseCancelEvent event = new CaseCancelEvent(identityProvider.getName(), caseId, caseFile, processInstanceIds);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseCancelled(e) );
        }
    }

    public void fireAfterCaseCancelled(String caseId, CaseFileInstance caseFile, List<Long> processInstanceIds) {
        if ( hasListeners() ) {
            final CaseCancelEvent event = new CaseCancelEvent(identityProvider.getName(), caseId, caseFile, processInstanceIds);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseCancelled(e) );
        }
    }
    
    /*
     * fire*CaseDestroyed
     */
    public void fireBeforeCaseDestroyed(String caseId, CaseFileInstance caseFile, List<Long> processInstanceIds) {
        if ( hasListeners() ) {
            final CaseDestroyEvent event = new CaseDestroyEvent(identityProvider.getName(), caseId, caseFile, processInstanceIds);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseDestroyed(e) );
        }
    }

    public void fireAfterCaseDestroyed(String caseId, CaseFileInstance caseFile, List<Long> processInstanceIds) {
        if ( hasListeners() ) {
            final CaseDestroyEvent event = new CaseDestroyEvent(identityProvider.getName(), caseId, caseFile, processInstanceIds);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseDestroyed(e) );
        }
    }
    
    /*
     * fire*CaseReopened
     */
    
    public void fireBeforeCaseReopened(String caseId, CaseFileInstance caseFile, String deploymentId, String caseDefinitionId, Map<String, Object> data) {
        if ( hasListeners() ) {
            final CaseReopenEvent event = new CaseReopenEvent(identityProvider.getName(), caseId, caseFile, deploymentId, caseDefinitionId, data);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseReopen(e) );
        }
    }

    public void fireAfterCaseReopened(String caseId, CaseFileInstance caseFile, String deploymentId, String caseDefinitionId, Map<String, Object> data, long processInstanceId) {
        if ( hasListeners() ) {
            final CaseReopenEvent event = new CaseReopenEvent(identityProvider.getName(), caseId, caseFile, deploymentId, caseDefinitionId, data, processInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseReopen(e) );
        }
    }
    
    /*
     * fire*CaseCommentAdded
     */
    public void fireBeforeCaseCommentAdded(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseCommentAdded(e) );
        }
    }
    
    public void fireAfterCaseCommentAdded(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseCommentAdded(e) );
        }
    }
    
    /*
     * fire*CaseCommentUpdated
     */
    public void fireBeforeCaseCommentUpdated(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseCommentUpdated(e) );
        }
    }
    
    public void fireAfterCaseCommentUpdated(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseCommentUpdated(e) );
        }
    }
    
    /*
     * fire*CaseCommentRemoved
     */
    public void fireBeforeCaseCommentRemoved(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseCommentRemoved(e) );
        }
    }
    
    public void fireAfterCaseCommentRemoved(String caseId, CaseFileInstance caseFile, CommentInstance commentInstance) {
        if ( hasListeners() ) {
            final CaseCommentEvent event = new CaseCommentEvent(identityProvider.getName(), caseId, caseFile, commentInstance);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseCommentRemoved(e) );
        }
    }
    
    /*
     * fire*CaseRoleAssignmentAdded
     */
    public void fireBeforeCaseRoleAssignmentAdded(String caseId, CaseFileInstance caseFile, String role, OrganizationalEntity entity) {
        if ( hasListeners() ) {
            final CaseRoleAssignmentEvent event = new CaseRoleAssignmentEvent(identityProvider.getName(), caseId, caseFile, role, entity);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseRoleAssignmentAdded(e) );
        }
    }
    
    public void fireAfterCaseRoleAssignmentAdded(String caseId, CaseFileInstance caseFile, String role, OrganizationalEntity entity) {
        if ( hasListeners() ) {
            final CaseRoleAssignmentEvent event = new CaseRoleAssignmentEvent(identityProvider.getName(), caseId, caseFile, role, entity);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseRoleAssignmentAdded(e) );
        }
    }
    
    /*
     * fire*CaseRoleAssignmentRemoved
     */
    public void fireBeforeCaseRoleAssignmentRemoved(String caseId, CaseFileInstance caseFile, String role, OrganizationalEntity entity) {
        if ( hasListeners() ) {
            final CaseRoleAssignmentEvent event = new CaseRoleAssignmentEvent(identityProvider.getName(), caseId, caseFile, role, entity);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseRoleAssignmentRemoved(e) );
        }
    }
    
    public void fireAfterCaseRoleAssignmentRemoved(String caseId, CaseFileInstance caseFile, String role, OrganizationalEntity entity) {
        if ( hasListeners() ) {
            final CaseRoleAssignmentEvent event = new CaseRoleAssignmentEvent(identityProvider.getName(), caseId, caseFile, role, entity);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseRoleAssignmentRemoved(e) );
        }
    }

    /*
     * fire*CaseDataAdded
     */
    public void fireBeforeCaseDataAdded(String caseId, CaseFileInstance caseFile, String definitionId, Map<String, Object> data) {
        if ( hasListeners() ) {
            final CaseDataEvent event = new CaseDataEvent(identityProvider.getName(), caseId, caseFile, definitionId, data);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseDataAdded(e) );
        }
    }
    
    public void fireAfterCaseDataAdded(String caseId, CaseFileInstance caseFile, String definitionId, Map<String, Object> data) {
        if ( hasListeners() ) {
            final CaseDataEvent event = new CaseDataEvent(identityProvider.getName(), caseId, caseFile, definitionId, data);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseDataAdded(e) );
        }
    }
    
    /*
     * fire*CaseDataRemoved
     */
    public void fireBeforeCaseDataRemoved(String caseId, CaseFileInstance caseFile, String definitionId, Map<String, Object> data) {
        if ( hasListeners() ) {
            final CaseDataEvent event = new CaseDataEvent(identityProvider.getName(), caseId, caseFile, definitionId, data);
            notifyAllListeners( event, ( l, e ) -> l.beforeCaseDataRemoved(e) );
        }
    }
    
    public void fireAfterCaseDataRemoved(String caseId, CaseFileInstance caseFile, String definitionId, Map<String, Object> data) {
        if ( hasListeners() ) {
            final CaseDataEvent event = new CaseDataEvent(identityProvider.getName(), caseId, caseFile, definitionId, data);
            notifyAllListeners( event, ( l, e ) -> l.afterCaseDataRemoved(e) );
        }
    }
    
    /*
     * fire*CaseDynamicTaskAdded
     */
    public void fireBeforeDynamicTaskAdded(String caseId, CaseFileInstance caseFile, long processInstanceId, String nodeType, Map<String, Object> parameters) {
        if ( hasListeners() ) {
            final CaseDynamicTaskEvent event = new CaseDynamicTaskEvent(identityProvider.getName(), caseId, caseFile, nodeType, parameters, processInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.beforeDynamicTaskAdded(e) );
        }
    }
    
    public void fireAfterDynamicTaskAdded(String caseId, CaseFileInstance caseFile, long processInstanceId, String nodeType, Map<String, Object> parameters) {
        if ( hasListeners() ) {
            final CaseDynamicTaskEvent event = new CaseDynamicTaskEvent(identityProvider.getName(), caseId, caseFile, nodeType, parameters, processInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.afterDynamicTaskAdded(e) );
        }
    }

    /*
     * fire*CaseDynamicProcessAdded
     */
    public void fireBeforeDynamicProcessAdded(String caseId, CaseFileInstance caseFile, long processInstanceId, String processId, Map<String, Object> parameters) {
        if ( hasListeners() ) {
            final CaseDynamicSubprocessEvent event = new CaseDynamicSubprocessEvent(identityProvider.getName(), caseId, caseFile, processId, parameters, processInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.beforeDynamicProcessAdded(e) );
        }
    }
    
    public void fireAfterDynamicProcessAdded(String caseId, CaseFileInstance caseFile, long processInstanceId, String processId, Map<String, Object> parameters, long subProcessInstanceId) {
        if ( hasListeners() ) {
            final CaseDynamicSubprocessEvent event = new CaseDynamicSubprocessEvent(identityProvider.getName(), caseId, caseFile, processId, parameters, processInstanceId, subProcessInstanceId);
            notifyAllListeners( event, ( l, e ) -> l.afterDynamicProcessAdded(e) );
        }
    }
    public void reset() {
        this.clear();
    }
}
