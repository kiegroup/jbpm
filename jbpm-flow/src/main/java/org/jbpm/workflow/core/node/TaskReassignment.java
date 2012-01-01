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

public class TaskReassignment implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String reassignUsers;
    
    private String reassignGroups;

    public void setReassignUsers(String reassignUsers) {
        this.reassignUsers = reassignUsers;
    }

    public String getReassignUsers() {
        return reassignUsers;
    }

    public void setReassignGroups(String reassignGroups) {
        this.reassignGroups = reassignGroups;
    }

    public String getReassignGroups() {
        return reassignGroups;
    }

}
