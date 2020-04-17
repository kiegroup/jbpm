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
package org.jbpm.process.instance.impl;

import java.io.Serializable;

import org.kie.api.runtime.process.ProcessContext;

public class SignalNodeInstanceAction implements Action, Serializable {
	
	private static final long serialVersionUID = 1L;
	
    private String signal;
	
    public SignalNodeInstanceAction(String signal) {
		super();
        this.signal = signal;
	}
	
	public void execute(ProcessContext context) throws Exception {
        context.getProcessInstance().signalEvent(signal, context.getNodeInstance().getId());
	}
	

}
