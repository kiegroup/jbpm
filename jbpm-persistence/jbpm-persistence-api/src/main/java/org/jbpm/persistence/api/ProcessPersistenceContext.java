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

package org.jbpm.persistence.api;

import java.util.List;

import org.drools.persistence.api.PersistenceContext;
import org.kie.internal.process.CorrelationKey;

public interface ProcessPersistenceContext
    extends
    PersistenceContext {

    List<Long> findAllProcessInstanceInfo();

    PersistentProcessInstance persist(PersistentProcessInstance processInstanceInfo);
    
    PersistentCorrelationKey persist(PersistentCorrelationKey correlationKeyInfo);
    
    PersistentProcessInstance findProcessInstanceInfo(Long processId);
    
    default void evict(PersistentProcessInstance processInstanceInfo) {
        
    }

    void remove(PersistentProcessInstance processInstanceInfo);

    List<Long> getProcessInstancesWaitingForEvent(String type);
    
    Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey);
}
