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
import java.util.HashMap;
import java.util.Map;

public class TaskNotification implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String type;
    
    private String recipients;
    
    private String groupRecipients;
    
    // corresponds to from field
    private String sender;
    
    // corresponds to replyTo field
    private String receiver;
    
    private Map<String, String> subjects = new HashMap<String, String>();
    
    private Map<String, String> bodies = new HashMap<String, String>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Map<String, String> getSubjects() {
        return subjects;
    }

    public void setSubjects(Map<String, String> subjects) {
        this.subjects = subjects;
    }

    public Map<String, String> getBodies() {
        return bodies;
    }

    public void setBodies(Map<String, String> bodies) {
        this.bodies = bodies;
    }

    public void addSubject(String locale, String subject) {
        this.subjects.put(locale, subject);
    }
    
    public void addBody(String locale, String body) {
        this.bodies.put(locale, body);
    }

    public void setGroupRecipients(String groupRecipients) {
        this.groupRecipients = groupRecipients;
    }

    public String getGroupRecipients() {
        return groupRecipients;
    }
}
