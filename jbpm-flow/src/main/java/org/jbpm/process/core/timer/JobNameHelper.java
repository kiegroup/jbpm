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

import org.drools.core.time.JobContext;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.jbpm.process.instance.timer.TimerManager.StartProcessJobContext;
import org.kie.api.runtime.EnvironmentName;

public class JobNameHelper {

    private JobNameHelper() {}

    public static String getJobName(JobContext ctx, long id) {
        return getJobName(ctx, getGroupName(ctx), id);
    }

    public static String getJobName(JobContext ctx, String groupName, long id) {
        String jobName;
        if (ctx instanceof ProcessJobContext) {
            ProcessJobContext processCtx = (ProcessJobContext) ctx;
            final String timerName = "-" + getTimerName (processCtx);
            if (processCtx instanceof StartProcessJobContext) {
                jobName = groupName + "-StartProcess-" + ((StartProcessJobContext) processCtx).getProcessId() + timerName;
            } else {
                jobName = processCtx.getSessionId() + "-" + processCtx.getProcessInstanceId() + timerName;
            }
        } else if (ctx instanceof NamedJobContext) {
            jobName = ((NamedJobContext) ctx).getJobName();
        } else {
            jobName = "Timer-" + ctx.getClass().getSimpleName() + "-" + id;
        }
        return jobName;
    }
    
    private static String getTimerName (ProcessJobContext ctx) {
        String timerName = ctx.getTimer().getName(); 
        return  (timerName != null && !timerName.isEmpty() ? timerName + "-" : "") + ctx.getTimer().getId();
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
}
