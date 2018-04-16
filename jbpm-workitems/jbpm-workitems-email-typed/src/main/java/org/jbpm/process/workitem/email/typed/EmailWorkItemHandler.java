/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
 *
 */

package org.jbpm.process.workitem.email.typed;

import org.drools.core.process.instance.impl.TypedWorkItemImpl;
import org.jbpm.process.workitem.core.AbstractLogOrThrowTypedWorkItemHandler;
import org.kie.api.runtime.process.TypedWorkItem;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;

/**
 * WorkItemHandler for sending email.
 * <p>
 * Expects the following parameters:
 * - "From" (String): sends an email from the given the email address
 * - "To" (String): sends the email to the given email address(es),
 * multiple addresses must be separated using a semi-colon (';')
 * - "Subject" (String): the subject of the email
 * - "Body" (String): the body of the email (using HTML)
 * - "Template" (String): optional template to generate body of the email, template when given will override Body parameter
 * Is completed immediately and does not return any result parameters.
 * <p>
 * Sending an email cannot be aborted.
 */
public class EmailWorkItemHandler extends AbstractLogOrThrowTypedWorkItemHandler<TypedWorkItem<Message, Object>> {

    private Connection connection;
    private TemplateManager templateManager = TemplateManager.get();

    public EmailWorkItemHandler() {
    }

    public EmailWorkItemHandler(String host, String port, String userName, String password) {
        setConnection(host, port, userName, password);
    }

    public EmailWorkItemHandler(String host, String port, String userName, String password, String startTls) {
        setConnection(host, port, userName, password, startTls);
    }

    @Override
    public TypedWorkItem<Message, Object> createTypedWorkItem() {
        return new TypedWorkItemImpl<>(new Message(), new Object());
    }

    public void setConnection(String host, String port, String userName, String password) {
        connection = new Connection();
        connection.setHost(host);
        connection.setPort(port);
        connection.setUserName(userName);
        connection.setPassword(password);
    }

    public void setConnection(String host, String port, String userName, String password, String startTls) {
        connection = new Connection();
        connection.setHost(host);
        connection.setPort(port);
        connection.setUserName(userName);
        connection.setPassword(password);
        connection.setStartTls(Boolean.parseBoolean(startTls));
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void executeWorkItem(TypedWorkItem<Message, Object> workItem, WorkItemManager manager) {
        try {
            Email email = createEmail(workItem, connection);
            SendHtml.sendHtml(email, false); // we can't get a debug flag from a pojo!
            // avoid null pointer when used from deadline escalation handler
            if (manager != null) {
                manager.completeWorkItem(workItem.getId(), null);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected Email createEmail(TypedWorkItem<Message, Object> workItem, Connection connection) {
        Email email = new Email();

        // setup email
        email.setMessage(workItem.getParameters());
        email.setConnection(connection);

        return email;
    }

    @Override
    public void abortWorkItem(TypedWorkItem<Message, Object> workItem, WorkItemManager manager) {

    }


}
