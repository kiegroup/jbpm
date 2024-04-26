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

package org.jbpm.process.audit.event;

import org.kie.api.event.process.ProcessDataChangedEvent;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessAsyncNodeScheduledEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;

public interface AuditEventBuilder {

    AuditEvent buildEvent(ProcessStartedEvent pse);
    
    AuditEvent buildEvent(ProcessCompletedEvent pce, Object log);
    
    AuditEvent buildEvent(ProcessNodeTriggeredEvent pnte);
    
    AuditEvent buildEvent(ProcessNodeTriggeredEvent pnte, Object log);
    
    AuditEvent buildEvent(ProcessNodeLeftEvent pnle, Object log);
    
    AuditEvent buildEvent(ProcessAsyncNodeScheduledEvent pnle);
    
    AuditEvent buildEvent(ProcessVariableChangedEvent pvce);

    AuditEvent buildEvent(ProcessDataChangedEvent pdce);
}
