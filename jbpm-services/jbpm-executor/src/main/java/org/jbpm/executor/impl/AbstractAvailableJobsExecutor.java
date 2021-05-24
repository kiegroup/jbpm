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

package org.jbpm.executor.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jbpm.executor.AsyncJobException;
import org.jbpm.executor.entities.ErrorInfo;
import org.jbpm.executor.entities.RequestInfo;
import org.jbpm.executor.impl.event.ExecutorEventSupport;
import org.jbpm.executor.impl.event.ExecutorEventSupportImpl;
import org.jbpm.process.core.async.AsyncExecutionMarker;
import org.kie.api.executor.Command;
import org.kie.api.executor.CommandCallback;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.Executor;
import org.kie.api.executor.ExecutorQueryService;
import org.kie.api.executor.ExecutorStoreService;
import org.kie.api.executor.Reoccurring;
import org.kie.api.executor.STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heart of the executor component - executes the actual tasks.
 * Handles retries and error management. Based on results of execution notifies
 * defined callbacks about the execution results.
 *
 */
public abstract class AbstractAvailableJobsExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAvailableJobsExecutor.class);
    protected int retries = Integer.parseInt(System.getProperty("org.kie.executor.retry.count", "3"));

    protected Map<String, Object> contextData = new HashMap<>();
       
    protected ExecutorQueryService queryService;
   
    protected ClassCacheManager classCacheManager;
   
    protected ExecutorStoreService executorStoreService;
    
    protected ExecutorEventSupport eventSupport = new ExecutorEventSupportImpl();
    
    protected Executor executor;

    public void setEventSupport(ExecutorEventSupport eventSupport) {
        this.eventSupport = eventSupport;
    }

    public void setQueryService(ExecutorQueryService queryService) {
        this.queryService = queryService;
    }    

    public void setClassCacheManager(ClassCacheManager classCacheManager) {
        this.classCacheManager = classCacheManager;
    }
    
	public void setExecutorStoreService(ExecutorStoreService executorStoreService) {
		this.executorStoreService = executorStoreService;
	}
         
	public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void executeGivenJob(RequestInfo request) {
        if (request == null) {
            return;
        }

        ClassLoader cl = ExecutorUtil.getClassLoader(request.getDeploymentId());
        CommandContext ctx = ExecutorUtil.toCommandContext(request.getRequestData(), cl);
        Command cmd = classCacheManager.findCommand(request.getCommandName(), cl);
        List<CommandCallback> callbacks = classCacheManager.buildCommandCallback(ctx, cl);

        logger.debug("Processing Request Id: {}, status {} command {}", request.getId(), request.getStatus(), request.getCommandName());
        boolean owner = ((ExecutorImpl) executor).getTransactionManager().begin();
        try {
            updateProcessInfoInContext(request, ctx);
            AsyncExecutionMarker.markAsync();
            eventSupport.fireBeforeJobExecuted(request, null);
            ExecutionResults results = executeCommand(request, cmd, ctx, cl);
            
            // reinit just in case there was a modification in the callbacks
            callbacks = classCacheManager.buildCommandCallback(ctx, cl);
            // request marked as done
            request.setResponseData(ExecutorUtil.toByteArray(results));
            request.setExecutions(request.getExecutions() + 1);
            request.setStatus(STATUS.DONE);
            executorStoreService.updateRequest(request, null);
            updateProcessInfoInContext(request, ctx);
            callbacks.stream().forEach(handler -> handler.onCommandDone(ctx, results));
            handleCompletionReocurringJobs(cmd, ctx);
            eventSupport.fireAfterJobExecuted(request, null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            request.setResponseData(null);
            request.setExecutions(request.getExecutions() + 1);
            request.setStatus(STATUS.QUEUED);
            executorStoreService.updateRequest(request, null);
            ((ExecutorImpl) executor).cancelRequest(request.getId());
            eventSupport.fireAfterJobExecuted(request, exception);
        } catch (Throwable exception) {
            handleException(request, exception, ctx, callbacks);
            eventSupport.fireAfterJobExecuted(request, exception);
        } finally {
            ((ExecutorImpl) executor).clearExecution(request.getId());
            AsyncExecutionMarker.reset();
            ((ExecutorImpl) executor).getTransactionManager().commit(owner);
        }
    }

    private ExecutionResults executeCommand(RequestInfo request, Command cmd, CommandContext ctx, ClassLoader cl) throws Exception {
        ExecutionResults results = null;
        if (request.getResponseData() == null) {
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                ctx.setData(entry.getKey(), entry.getValue());
            }
            // add class loader so internally classes can be created with valid (kjar) deployment
            ctx.setData("ClassLoader", cl);
            // increment execution counter directly to cover both success and failure paths
            results = cmd.execute(ctx);
            if (results == null) {
                results = new ExecutionResults();
            }
        } else {
            results = ExecutorUtil.toExecutionResult(request.getResponseData(), cl);
        }
        results.setData("CompletedAt", new Date());
        return results;
    }


    public void addContextData(String name, Object data) {
        this.contextData.put(name, data);
    }


    protected void handleException(RequestInfo request, Throwable e, CommandContext ctx, List<CommandCallback> callbacks) {
        logger.warn("Error during command {} error message {}", request.getCommandName(), e.getMessage(), e);
        ErrorInfo errorInfo = new ErrorInfo(e.getMessage(), ExceptionUtils.getStackTrace(e.fillInStackTrace()));
        errorInfo.setRequestInfo(request);
        executorStoreService.persistError(errorInfo);
        logger.info("Error Number: {}", request.getErrorInfo().size());
        if (request.getRetries() > 0) {
            logger.debug("Retrying ({}) still available for requests {} !", request.getRetries(), request.getId());

            request.setStatus(STATUS.RETRYING);
            request.setRetries(request.getRetries() - 1);
            request.setExecutions(request.getExecutions() + 1);
            request.setTime(computeRetryDelay(ctx, request.getExecutions()));
            logger.info("Retrying request {} - delay configured, next retry at {} with executions {}", request.getId(), request.getTime(), request.getExecutions());

            executorStoreService.updateRequest(request, (T) -> ((ExecutorImpl) executor).scheduleExecution(request, request.getTime()));
        } else {
            logger.debug("No retries left for request {}!", request.getId());
            request.setExecutions(request.getExecutions() + 1);
            request.setStatus(STATUS.ERROR);
            executorStoreService.updateRequest(request, null);

            AsyncJobException wrappedException = new AsyncJobException(request.getId(), request.getCommandName(), e);
            callbacks.stream().forEach(handler -> handler.onCommandError(ctx, wrappedException));
        }
    }
    
    private Date computeRetryDelay(CommandContext ctx, int executions) {
        // calculate next retry time
        List<Long> retryDelay = (List<Long>) ctx.getData("retryDelay");
        if (retryDelay == null) {
            return new Date();
        }

        long retryAdd = 0l;

        try {
            retryAdd = retryDelay.get(executions - 1); // need to decrement it as executions are directly incremented upon execution
        } catch (IndexOutOfBoundsException ex) {
            // in case there is no element matching given execution, use last one
            retryAdd = retryDelay.get(retryDelay.size()-1);
        }
        Date newTime = new Date(System.currentTimeMillis() + retryAdd);
        return newTime;
    }

    protected void handleCompletionReocurringJobs(Command cmd, CommandContext ctx) {
        if (cmd == null || !(cmd instanceof Reoccurring)) {
            return;
        }

        Date current = new Date();
        Date nextScheduleTime = ((Reoccurring) cmd).getScheduleTime();
        if (nextScheduleTime == null || !nextScheduleTime.after(current)) {
            return;
        }

        String businessKey = (String) ctx.getData("businessKey");
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCommandName(cmd.getClass().getName());
        requestInfo.setKey(businessKey);
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setTime(nextScheduleTime);
        requestInfo.setMessage("Rescheduled reoccurring job");
        requestInfo.setDeploymentId((String)ctx.getData("deploymentId"));
        requestInfo.setProcessInstanceId((Long)ctx.getData("processInstanceId"));
        requestInfo.setOwner((String)ctx.getData("owner"));
        if (ctx.getData("retries") != null) {
            requestInfo.setRetries(Integer.valueOf(String.valueOf(ctx.getData("retries"))));
        } else {
            requestInfo.setRetries(retries);
        }
        ctx.getData().remove("ClassLoader");
        requestInfo.setRequestData(ExecutorUtil.toByteArray(ctx));
        eventSupport.fireBeforeJobScheduled(requestInfo, null);
        executorStoreService.persistRequest(requestInfo, (ri) -> ((ExecutorImpl) executor).scheduleExecution(requestInfo, requestInfo.getTime()));
        eventSupport.fireAfterJobScheduled(requestInfo, null);

    }

    protected void updateProcessInfoInContext(RequestInfo requestInfo, CommandContext ctx) {
        
        if (requestInfo.getDeploymentId() != null) {
            ctx.setData("deploymentId", requestInfo.getDeploymentId());
        }
        
        if (requestInfo.getProcessInstanceId() != null) {
            ctx.setData("processInstanceId", requestInfo.getProcessInstanceId());
        }
    }
}
