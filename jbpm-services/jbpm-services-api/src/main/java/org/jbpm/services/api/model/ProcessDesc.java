/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.api.model;

import java.util.List;
import java.util.Map;

public interface ProcessDesc extends DeployedAsset {

	/**
	 * Return the process description package name.
	 * @return package name
	 */
	String getPackageName();

	/**
	 * Return the process description namespace.
	 * @return namespace
	 */
	String getNamespace();

	/**
	 * Return the process description deployment id.
	 * @return deployment id
	 */
	String getDeploymentId();

	/**
	 * Return the encoded process source.
	 * @return encoded process source
	 */
	String getEncodedProcessSource();

	/**
	 * Return the process description forms.
	 * @return forms
	 */
	Map<String, String> getForms();

	/**
	 * Return the process description roles.
	 * @return roles
	 */
	List<String> getRoles();
}
