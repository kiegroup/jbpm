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

package org.jbpm.services.api.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.kie.api.task.model.OrganizationalEntity;

public interface TaskNotification extends Serializable {

    /**
     * Returns unique id of the task notification
     * @return task notification id
     */
    Long getId();
    
    /**
     * Returns optional name of the notification
     * @return notification name
     */
    String getName();
    
    /**
     * Returns date that the notification is schedule to happen
     * @return time notification is scheduled
     */
    Date getDate();
    
    /**
     * Returns list of recipients that notification will be sent to
     * @return list of recipients
     */
    List<OrganizationalEntity> getRecipients();
    
    /**
     * Returns list of business admins that notification will be sent to
     * @return business admin list
     */
    List<OrganizationalEntity> getBusinessAdministrators();
    
    /**
     * Returns the subject line for this notification
     * @return notification subject
     */
    String getSubject();
    
    /**
     * Returns the content for this notification
     * @return notification content
     */
    String getContent();
    
    /**
     * Returns if given notification is active
     * @return active
     */
    boolean isActive();

}
