/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.kie.services.impl.bpmn2;

import org.jbpm.bpmn2.services.impl.BPMN2DefinitionServiceImpl;
import org.jbpm.services.api.DeploymentEvent;
import org.jbpm.services.api.DeploymentEventListener;


public class BPMN2DeploymentDataServiceImpl extends BPMN2DefinitionServiceImpl implements DeploymentEventListener {

	@Override
	public void onDeploy(DeploymentEvent event) {
		// no op
	}

	@Override
	public void onUnDeploy(DeploymentEvent event) {
		// remove process definitions from the cache
		definitionCache.remove(event.getDeploymentId());
	}

	@Override
	public void onActivate(DeploymentEvent event) {
		// no op
	}

	@Override
	public void onDeactivate(DeploymentEvent event) {
		// no op
	}

}
