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

package org.jbpm.services.task.events;

import java.util.List;
import java.util.Map;

import org.drools.core.event.AbstractEventSupport;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskLifeCycleEventListener.AssignmentType;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;

public class TaskEventSupport extends AbstractEventSupport<TaskLifeCycleEventListener> {

    public void fireBeforeTaskActivated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskActivatedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskClaimed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskClaimedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskSkipped(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskSkippedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskStarted(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskStartedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskStopped(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskStoppedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskCompleted(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskCompletedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskFailed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskFailedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskAdded(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskAddedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskExited(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskExitedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskReleased(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskReleasedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskResumed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskResumedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskSuspended(final Task task, TaskContext context, Map<String, Object> parameters) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            event.getMetadata().putAll(parameters);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskSuspendedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskForwarded(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskForwardedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskDelegated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskDelegatedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskNominated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskNominatedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskUpdated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskUpdatedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskReassigned(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskReassignedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskNotified(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskNominatedEvent( e ) );
        }
    }
    
    public void fireBeforeTaskInputVariablesChanged(final Task task, TaskContext context, Map<String, Object> variables) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskInputVariableChangedEvent( e, variables ) );
        }
    }
    
    
    public void fireBeforeTaskOutputVariablesChanged(final Task task, TaskContext context, Map<String, Object> variables) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskOutputVariableChangedEvent( e, variables ) );
        }
    }
    
    public void fireBeforeTaskAssignmentsAddedEvent(final Task task, TaskContext context, AssignmentType type, List<OrganizationalEntity> entities) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskAssignmentsAddedEvent(e, type, entities) );
        }
    }
    
    public void fireBeforeTaskAssignmentsRemovedEvent(final Task task, TaskContext context, AssignmentType type, List<OrganizationalEntity> entities) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.beforeTaskAssignmentsRemovedEvent(e, type, entities) );
        }
    }

    
    // after methods
    
    public void fireAfterTaskActivated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskActivatedEvent(e) );
        }
    }
    
    public void fireAfterTaskClaimed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskClaimedEvent(e) );
        }
    }
    
    public void fireAfterTaskSkipped(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskSkippedEvent(e) );
        }
    }
    
    public void fireAfterTaskStarted(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskStartedEvent(e) );
        }
    }
    
    public void fireAfterTaskStopped(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskStoppedEvent(e) );
        }
    }
    
    public void fireAfterTaskCompleted(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskCompletedEvent(e) );
        }
    }
    
    public void fireAfterTaskFailed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskFailedEvent(e) );
        }
    }
    
    public void fireAfterTaskAdded(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskAddedEvent(e) );
        }
    }
    
    public void fireAfterTaskExited(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskExitedEvent(e) );
        }
    }
    
    public void fireAfterTaskReleased(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskReleasedEvent(e) );
        }
    }
    
    public void fireAfterTaskResumed(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskResumedEvent(e) );
        }
    }
    
    public void fireAfterTaskSuspended(final Task task, TaskContext context, Map<String, Object> parameters) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            event.getMetadata().putAll(parameters);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskSuspendedEvent(e) );
        }
    }
    
    public void fireAfterTaskForwarded(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskForwardedEvent(e) );
        }
    }
    
    public void fireAfterTaskDelegated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskDelegatedEvent(e) );
        }
    }
    
    public void fireAfterTaskNominated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskNominatedEvent(e) );
        }
    }
    
    public void fireAfterTaskUpdated(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskUpdatedEvent(e) );
        }
    }
    
    public void fireAfterTaskReassigned(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskReassignedEvent(e) );
        }
    }
    
    public void fireAfterTaskNotified(final Task task, TaskContext context) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskNotificationEvent(e) );
        }
    }
    
    public void fireAfterTaskInputVariablesChanged(final Task task, TaskContext context, Map<String, Object> variables) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskInputVariableChangedEvent(e, variables) );
        }
    }
    
    
    public void fireAfterTaskOutputVariablesChanged(final Task task, TaskContext context, Map<String, Object> variables) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskOutputVariableChangedEvent(e, variables) );
        }
    }
    
    public void fireAfterTaskAssignmentsAddedEvent(final Task task, TaskContext context, AssignmentType type, List<OrganizationalEntity> entities) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskAssignmentsAddedEvent(e, type, entities) );
        }
    }
    
    public void fireAfterTaskAssignmentsRemovedEvent(final Task task, TaskContext context, AssignmentType type, List<OrganizationalEntity> entities) {
        if ( hasListeners() ) {
            final TaskEventImpl event = new TaskEventImpl(task, context);
            notifyAllListeners( event, ( l, e ) -> l.afterTaskAssignmentsRemovedEvent(e, type, entities) );
        }
    }
}
