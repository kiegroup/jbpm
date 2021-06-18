/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.api.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


public interface ProcessDefinition extends Serializable, DeployedAsset {

    /**
     * Return the process definition id.
     * @return id
     */
    String getId();

    /**
     * Return the process definition name.
     * @return name
     */
    String getName();

    /**
     * Return the process definition version.
     * @return version
     */
    String getVersion();

    /**
     * Return the process definition package name.
     * @return package nem
     */
    String getPackageName();

    /**
     * Return the process definition type.
     * @return type
     */
    String getType();

    /**
     * Return the process definition deployment id.
     * @return deployment id
     */
    String getDeploymentId();

    /**
     * Return if process definition is dynamic.
     * @return dynamic
     */
    boolean isDynamic();

    /**
     * Returns process definition assoaciated entries.
     * @return associated entries
     */
    Map<String, Collection<String>> getAssociatedEntities();

    /**
     * Returns process definition service tasks
     * @return service tasks
     */
    Map<String, String> getServiceTasks();

    /**
     * Returns process definition process variables.
     * @return process variables
     */
    Map<String, String> getProcessVariables();

    /**
     * Returns process definition reusable subprocesses.
     * @return reusable subprocesses
     */
    Collection<String> getReusableSubProcesses();

    /**
     * Returns process definition signals names.
     * @return id of the signals defined in the process
     */
    Collection<String> getSignals();

    /**
     * Returns process definition signals.
     * @return information about the signals
     */
    Collection<SignalDesc> getSignalsDesc();

    /**
     * Returns process definition signals.
     * @return information about the signals
     */
    Collection<MessageDesc> getMessagesDesc();

    /**
     * Returns process definition globals.
     * @return globals
     */
    Collection<String> getGlobals();

    /**
     * Returns process definition referenced rules.
     * @return referenced rules
     */
    Collection<String> getReferencedRules();

    /**
     * Returns if process definition is active.
     * @return signals
     */
    boolean isActive();

    /**
     * Returns process definition nodes.
     * @return @{@link NodeDesc} nodes
     */
    Set<NodeDesc> getNodes();

    /**
     * Returns process definition timers.
     * @return @{@link TimerDesc} timers
     */
    Set<TimerDesc> getTimers();

    /**
     * Return the list of tags for certain process variable
     * @return tags names
     */
    Set<String> getTagsForVariable(String varName);

    /**
     * Return the list of tags
     * @return tags names with variable name
     */
    Map<String, Set<String>> getTagsInfo();
}
