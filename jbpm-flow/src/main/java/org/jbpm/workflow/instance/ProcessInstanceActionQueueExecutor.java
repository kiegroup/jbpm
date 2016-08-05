/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workflow.instance;

import java.util.Queue;
import java.util.Stack;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.workflow.instance.impl.queue.AfterInternalTriggerAction;
import org.jbpm.workflow.instance.impl.queue.NodeInstanceTriggerAction;
import org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction;
import org.jbpm.workflow.instance.impl.queue.SignalEventAction;
import org.jbpm.workflow.instance.node.queue.ComplexInternalTriggerNodeInstance;
import org.kie.api.runtime.process.EventSignallable;

/**
 * An implementation of this class contains a collection of {@link ProcessInstanceAction} instances that it can execute.
 */
public interface ProcessInstanceActionQueueExecutor {

    /**
     * Adds a {@link NodeInstanceTriggerAction} instance to the queue to be executed
     * @param nodeInstance The {@link NodeInstance} on which {@link NodeInstance#trigger(org.kie.api.runtime.process.NodeInstance, String)}
     *                     will be called on execution.
     * @param from The {@link NodeInstance} from which this being called
     * @param type The connection type
     */
    void addNodeInstanceTriggerAction( NodeInstance nodeInstance, NodeInstance from, String type );

    /**
     * Adds a {@link SignalEventAction} instance to the the queue be to be executed
     *
     * @param eventSignallable An {@link EventSignallable} implementation that {@link EventSignallable#signalEvent(String, Object)}
     *  will be called on execution.
     * @param type The signal type
     * @param event The signal content ("event")
     */
    void addSignalEventAction( EventSignallable eventSignallable, String type, Object event );


    /**
     * Adds a {@link AfterInternalTriggerAction} instance to the queue to be executed.
     * @param nodeInstance A {@link ComplexInternalTriggerNodeInstance} instance ton which the {@link ComplexInternalTriggerNodeInstance#afterInternalTrigger()}
     * method will be called on execution.
     */
    void addAfterInternalTriggerAction( ComplexInternalTriggerNodeInstance nodeInstance );

    /**
     * Adds a {@link ProcessInstanceAction} implementation instance to the queue to be executed
     * @param processInstanceAction
     */
    void addProcessInstanceAction( ProcessInstanceAction processInstanceAction );


    /**
     * Removes a {@link ProcessInstanceAction}: this method is primarily used when a {@link NodeInstance} is canceled and thus
     * removed. Removing/cancelling a {@link NodeInstance} of course means that any associated {@link ProcessInstanceAction} should also not be executed.
     *
     * @param implInstance The {@link ProcessImplementationPart} (most often a {@link ProcessInstance} or {@link NodeInstance}) which the
     * associated {@link ProcessInstanceAction} is based on.
     *
     * @see ProcessInstanceAction#actsOn(ProcessImplementationPart)
     */
    void removeAssociatedAction( ProcessImplementationPart implInstance );

    /**
     * The internal "queue" of {@link ProcessInstanceAction} instances is actually a {@link Stack} of {@link Queue}s. Adding
     * a new {@link Queue} to this internal {@link Stack} is the primary way to scope {@link ProcessInstanceAction}s
     * </p>
     * For example, there may be {@link ProcessInstanceAction}s that should be executed <em>after</em> a subprocess has completed, instead
     * of immediately after the first action. Stacking {@link Queue}s allows to implement this without having to rely on the old
     * recursive logic.
     * @param forceNewQueue When this is false, a new queue is only added to the stack
     */
    void addNewExecutionQueueToStack(boolean forceNewQueue);

    /**
     * This method will initiate execution of the queue of {@link ProcessInstanceAction}s -- but <em>only</em> if execution has
     * <b>not</b> already started.
     * </p>
     * The reason for this is that there are multiple entry-points to process instance (a normal start, a signal event, or a work item completion, to name 3),
     * and that it is not always possible in the code to know if the process instance is already executing or not.
     */
    void executeQueue();

    /**
     * If the process instance is aborted, this immediately stops execution.
     */
    void abortQueueExecution();

}
