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
package org.jbpm.process.core.timer;

import java.util.Collection;

import org.drools.core.time.JobContext;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.jbpm.process.instance.timer.TimerManager.StartProcessJobContext;
import org.jbpm.workflow.core.node.StateBasedNode;
import org.jbpm.workflow.instance.NodeInstanceContainer;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.definition.process.Node;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.process.NodeInstance;

public class JobNameHelper {

    private JobNameHelper() {}

    public static String getJobName(JobContext ctx, long id) {
        return getJobName(ctx, getGroupName(ctx), id);
    }

    public static String getJobName(JobContext ctx, String groupName, long id) {
        String jobName;
        if (ctx instanceof ProcessJobContext) {
            ProcessJobContext processCtx = (ProcessJobContext) ctx;
            long timerId = processCtx.getTimer().getTimerId();
            if (processCtx instanceof StartProcessJobContext) {
                jobName = groupName + "-StartProcess-" + ((StartProcessJobContext) processCtx).getProcessId() + timerId;
            } else {
                String timerName = null;
                for (NodeInstance nodeInstance : ((NodeInstanceContainer) processCtx.getKnowledgeRuntime().getProcessInstance(processCtx.getProcessInstanceId())).getNodeInstances()) {
                    Node foundNode = null;
                    if (((NodeInstanceImpl) nodeInstance).getSlaTimerId() == timerId) {
                        foundNode = nodeInstance.getNode();
                    }
                    else if (nodeInstance instanceof TimerNodeInstance) {
                        if (((TimerNodeInstance) nodeInstance).getTimerId() == timerId) {
                            foundNode = nodeInstance.getNode();
                        }
                    }
                    else if (nodeInstance instanceof StateBasedNodeInstance) {
                        foundNode = ((StateBasedNode) nodeInstance.getNode()).getBoundaryNode(processCtx.getTimer().getId());
                    }
                    if (foundNode != null) {
                        timerName = TimerNameHelper.getTimerName(nodeInstance, foundNode);
                        break;
                    }
                }
                jobName = processCtx.getSessionId() + "-" + processCtx.getProcessInstanceId() + "-" + (timerName != null && !timerName.isEmpty() ? timerName + "-" : "") + processCtx.getTimer().getId();
            }
        } else if (ctx instanceof NamedJobContext) {
            jobName = ((NamedJobContext) ctx).getJobName();

        } else {
            jobName = "Timer-" + ctx.getClass().getSimpleName() + "-" + id;
        }
        return jobName;
    }

    public static String getGroupName(JobContext ctx) {
        String groupName = "jbpm";
        if (ctx instanceof ProcessJobContext) {
            ProcessJobContext processCtx = (ProcessJobContext) ctx;
            String deploymentId = (String) processCtx.getKnowledgeRuntime().getEnvironment().get(EnvironmentName.DEPLOYMENT_ID);
            if (deploymentId != null) {
                groupName = deploymentId;
            }
        } else if (ctx instanceof NamedJobContext) {
            String deploymentId = ((NamedJobContext) ctx).getDeploymentId();
            if (deploymentId != null) {
                groupName = deploymentId;
            }
        }
        return groupName;
    }

    private static <T> boolean safeContains(Collection<T> items, T item) {
        return items != null && items.contains(item);
    }
}
