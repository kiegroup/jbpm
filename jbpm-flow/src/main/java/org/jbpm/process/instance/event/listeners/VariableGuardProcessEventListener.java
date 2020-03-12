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

package org.jbpm.process.instance.event.listeners;

import org.jbpm.process.core.context.variable.VariableViolationException;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.internal.identity.IdentityProvider;


public class VariableGuardProcessEventListener extends DefaultProcessEventListener {

    private String tag;
    private String requiredRole;
    private IdentityProvider identityProvider;
        
    public VariableGuardProcessEventListener(String requiredRole, IdentityProvider identityProvider) {        
        this("restricted", requiredRole, identityProvider);
    }

    public VariableGuardProcessEventListener(String tag, String requiredRole, IdentityProvider identityProvider) {
        this.tag = tag;
        this.requiredRole = requiredRole;
        this.identityProvider = identityProvider;
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        if (event.hasTag(tag) && !identityProvider.hasRole(requiredRole)) {
            throw new VariableViolationException(event.getProcessInstance().getId(), event.getVariableId(), "Variable is restricted to only '" + requiredRole + "' role");
        }
    }

}
