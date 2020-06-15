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

import java.util.Date;

public interface UserTaskInstanceDesc {

	/**
	 * Returns the user task instance task id.
	 * @return task id
	 */
	Long getTaskId();

	/**
	 * Returns the user task instance status.
	 * @return status
	 */
	String getStatus();

	/**
	 * Returns the user task instance activation time.
	 * @return activation time
	 */
	Date getActivationTime();

	/**
	 * Returns the user task instance name.
	 * @return name
	 */
	String getName();

	/**
	 * Returns the user task instance description.
	 * @return description
	 */
	String getDescription();

	/**
	 * Returns the user task instance priority.
	 * @return priority
	 */
	Integer getPriority();

	/**
	 * Returns the user task instance created by.
	 * @return created by
	 */
	String getCreatedBy();

	/**
	 * Returns the user task instance created on date.
	 * @return created on
	 */
	Date getCreatedOn();

	/**
	 * Returns the user task instance due date.
	 * @return due date
	 */
	Date getDueDate();

	/**
	 * Returns the user task instance process instance id.
	 * @return process instance id
	 */
	Long getProcessInstanceId();

	/**
	 * Returns the user task process id.
	 * @return process id
	 */
	String getProcessId();

	/**
	 * Returns the user task actual owner.
	 * @return actual owner
	 */
	String getActualOwner();

	/**
	 * Returns the user task deployment id.
	 * @return deployment id
	 */
	String getDeploymentId();

	/**
	 * Returns the user task form name.
	 * @return form name
	 */
	String getFormName();

	/**
	 * Returns the user task workitem id.
	 * @return workitem id
	 */
	Long getWorkItemId();

	/**
	 * Returns the user task SLA compliance.
	 * @return SLA compliance
	 */
	Integer getSlaCompliance();

	/**
	 * Returns the user task SLA due date.
	 * @return SLA due date
	 */
	Date getSlaDueDate();

	/**
	 * Set the user task SLA compliance.
	 * @param slaCompliance SLA compliance value
	 */
	void setSlaCompliance(Integer slaCompliance);

	/**
	 * Set the user task SLA due date.
	 * @param slaDueDate SLA due date
	 */
	void setSlaDueDate(Date slaDueDate);

	/**
	 * * Returns task subject 
	 * @return task subject
	 */
	String getSubject();

	/**
	 * Set task subject 
	 * @param subject task subject
	 */
	void setSubject(String subject);

	/**
	 * Returns correlation key
	 * @return correlation key
	 */
	String getCorrelationKey();

	/**
	 * Sets correlation key
	 * @param correlationKey
	 */
	void setCorrelationKey(String correlationKey);

	/**
	 * Returns process type
	 * @return 1 if process, 2 if case
	 */
	Integer getProcessType();

	/**
	 * Set process type
	 * @param process type (1 for process, 2 for case)
	 */
	void setProcessType(Integer processType);
}
