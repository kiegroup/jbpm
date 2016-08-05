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

package org.jbpm.ruleflow.instance;

import static org.kie.api.runtime.EnvironmentName.USE_QUEUE_BASED_EXECUTION;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jbpm.process.instance.AbstractProcessInstanceFactory;
import org.jbpm.process.instance.ProcessInstance;
import org.kie.api.runtime.Environment;

public class RuleFlowProcessInstanceFactory extends AbstractProcessInstanceFactory implements Externalizable {

    private static final long serialVersionUID = 510l;

    protected static final boolean SYSTEM_QUEUE_BASED_EXECUTION_PROPERTY = Boolean.getBoolean("org.jbpm.exec.queue");

    public ProcessInstance createProcessInstance(Environment env) {
        RuleFlowProcessInstance processInstance = new RuleFlowProcessInstance();
        boolean queueBasedExecution = useQueueBasedExecution(env);
        processInstance.setQueueBased(queueBasedExecution);
        return processInstance;
    }

    /**
     * Determining queue based execution:
     *
     * in 6.x, 7.x:  default is FALSE
     * 1. if env == null: FALSE
     * 2. else if env variable == true, TRUE
     * 3. else if system prop == true, TRUE
     *
     * in 8.x: default is TRUE
     * 1. if env == null -> TRUE
     * 2. else if env variable == FALSE ->  FALSE
     * 3. else if system prop == FASLE -> FASLE
     *
     * @param env
     * @return a {@link boolean}
     */
    public static boolean useQueueBasedExecution(Environment env) {
        boolean queueBasedExecution;
        if( env == null ) {
            queueBasedExecution = false;
        } else {
            Object queueBasedBoolObj = env.get(USE_QUEUE_BASED_EXECUTION);
            if( queueBasedBoolObj instanceof Boolean ) { // null check
                queueBasedExecution = (Boolean) queueBasedBoolObj;
            } else {
                queueBasedExecution = SYSTEM_QUEUE_BASED_EXECUTION_PROPERTY;
            }
        }
        return queueBasedExecution;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }


}
