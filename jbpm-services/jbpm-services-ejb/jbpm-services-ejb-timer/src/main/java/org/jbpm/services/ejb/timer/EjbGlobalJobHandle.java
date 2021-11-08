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

package org.jbpm.services.ejb.timer;

import org.jbpm.process.core.timer.impl.GlobalTimerService.GlobalJobHandle;

public class EjbGlobalJobHandle extends GlobalJobHandle {
	
	private static final long serialVersionUID = 4254413497038652954L;
	private String uuid;
	private String deploymentId;
	
	public EjbGlobalJobHandle(long id, String uuid, String deploymentId) {
		super(id);
		this.uuid = uuid;
		this.deploymentId = deploymentId;
	}

	@Override
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "EjbGlobalJobHandle [uuid=" + uuid + "]";
	}

	public String getDeploymentId() {
		return deploymentId;
	}

	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

	
}
