/**
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collection;

import org.jbpm.workflow.instance.impl.factory.ReuseNodeFactory;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;

/**
 *
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public interface NodeInstanceContainer extends org.kie.api.runtime.process.NodeInstanceContainer {

    Collection<NodeInstance> getNodeInstances(boolean recursive);

    NodeInstance getNodeInstanceRecursively(long nodeInstanceId);

    /**
     * Returns the first node instance (of a container-like node). The
     * {@link NodeInstance} will be created if it does not already exist.
     * </p>
     * This method should only be used by methods that end up providing
     * a {@link NodeInstanceAction}.
     * </p>
     * The returned {@link NodeInstance} should never be directly triggerd.
     *
     * @param nodeId The node id
     * @return A {@link NodeInstance} corresponding to the node id
     * @see {@link ReuseNodeFactory#getNodeInstance(Node, WorkflowProcessInstance, org.kie.api.runtime.process.NodeInstanceContainer)},
     * {@link ForEachNodeInstance#createNodeInstance(Node)}
     */
    NodeInstance getFirstNodeInstance(long nodeId);

    /**
     * Creates (or uses a factory to create) a new {@link NodeInstance} based
     * on the given Node.
     * </p>
     * This method should only be used by methods that end up providing
     * a {@link NodeInstanceAction}.
     * </p>
     * The returned {@link NodeInstance} should never be triggered directly.
     * @param node
     * @return
     */
    NodeInstance createNodeInstance(Node node);

    /**
     * Add new node instance to this instance's list of {@link NodeInstance}s
     * @param nodeInstance
     */
    void addNodeInstance(NodeInstance nodeInstance);

    void removeNodeInstance(NodeInstance nodeInstance);

    NodeContainer getNodeContainer();

    void nodeInstanceCompleted(NodeInstance nodeInstance, String outType);

    int getState();

    void setState(int state);

    int getLevelForNode(String uniqueID);

    void setCurrentLevel(int level);

    int getCurrentLevel();

}
