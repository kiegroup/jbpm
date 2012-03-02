/**
 * Copyright 2011 JBoss Inc
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
package org.jbpm.workflow.core.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskDeadline implements Serializable {

    private static final long serialVersionUID = 1L;

    // either start or completed dead line are supported
    private String type;
    
    private String expires;
    
    private List<TaskNotification> notifications = new ArrayList<TaskNotification>();
    
    private List<TaskReassignment> reassignments = new ArrayList<TaskReassignment>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public List<TaskNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<TaskNotification> notifications) {
        this.notifications = notifications;
    }

    public List<TaskReassignment> getReassignments() {
        return reassignments;
    }

    public void setReassignments(List<TaskReassignment> reassignments) {
        this.reassignments = reassignments;
    }
    
    public void addNotification(TaskNotification notification) {
        this.notifications.add(notification);
    }
    
    public void addReassignment(TaskReassignment reassignment) {
        this.reassignments.add(reassignment);
    }
}
