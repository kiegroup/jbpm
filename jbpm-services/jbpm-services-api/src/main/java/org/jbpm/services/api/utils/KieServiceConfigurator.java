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

package org.jbpm.services.api.utils;

import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.query.QueryService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;

/**
 * Configurator for kie service.
 */
public interface KieServiceConfigurator {

    /**
     * Configures a new kie service.
     * @param puName
     * @param identityProvider
     * @param userGroupCallback
     */
    void configureServices(String puName, IdentityProvider identityProvider, UserGroupCallback userGroupCallback);

    /**
     * Created a new deployment unit
     * @param groupId
     * @param artifactid
     * @param version
     * @return @{@link DeploymentUnit} deployment unit
     */
    DeploymentUnit createDeploymentUnit(String groupId, String artifactid, String version);

    /**
     * Closes the configurator.
     */
    void close();

    /**
     * Returns the deployment service.
     * @return @{@link DeploymentService} deployment service
     */
    DeploymentService getDeploymentService();

    /**
     * Returns the definition service.
     * @return @{@link DefinitionService} definition service
     */
    DefinitionService getBpmn2Service();

    /**
     * Returns the runtime data service.
     * @return @{@link RuntimeDataService} runtime data service
     */
    RuntimeDataService getRuntimeDataService();

    /**
     * Return related data regarding variables (case, process, task) 
     * @return @{@link AdvanceRuntimeDataService}
     */
    AdvanceRuntimeDataService getAdvanceVariableDataService();

    /**
     * Returns the process service.
     * @return @{@link ProcessService} process service
     */
    ProcessService getProcessService();

    /**
     * Returns the user task service.
     * @return @{@link UserTaskService} user task service
     */
    UserTaskService getUserTaskService();

    /**
     * Returns the query service
     * @return @{@link QueryService} query service
     */
    QueryService getQueryService();

    /**
     * Returns the process instance admin service
     * @return @{@link ProcessInstanceAdminService} process intstance admin service
     */
    ProcessInstanceAdminService getProcessAdminService();

    /**
     * Returns the identity provider.
     * @return @{@link IdentityProvider} identity provider
     */
    IdentityProvider getIdentityProvider();

    /**
     * Returns the user group callback.
     * @return @{@link UserGroupCallback} user group callback
     */
    UserGroupCallback getUserGroupCallback();

}