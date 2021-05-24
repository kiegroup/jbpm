/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.impl;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.kie.api.executor.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSTaskExecutorStrategy implements TaskExecutorStrategy {
    // jms related instances
    private final boolean useJMS = Boolean.parseBoolean(System.getProperty("org.kie.executor.jms", "true"));

    private static final Logger logger = LoggerFactory.getLogger(JMSTaskExecutorStrategy.class);

    private String connectionFactoryName = System.getProperty("org.kie.executor.jms.cf", "java:/JmsXA");
    private String queueName = System.getProperty("org.kie.executor.jms.queue", "queue/KIE.EXECUTOR");
    private boolean transacted = Boolean.parseBoolean(System.getProperty("org.kie.executor.jms.transacted", "false"));
    private ConnectionFactory connectionFactory;
    private Queue queue;
    private boolean active;

    @Override
    public void init() {
        if(!useJMS) {
            return;
        }
        try {
            InitialContext ctx = new InitialContext();
            if (this.connectionFactory == null) {
                this.connectionFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
            }
            if (this.queue == null) {
                this.queue = (Queue) ctx.lookup(queueName);
            }
            active = true;
            logger.info("Executor JMS based support successfully activated on queue {}", queue);
        } catch (Exception e) {
            logger.warn("Disabling JMS support in executor because: unable to initialize JMS configuration for executor due to {}", e.getMessage());
            logger.debug("JMS support executor failed due to {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean active() {
        return active;
    }

    @Override
    public void destroy() {
        active = false;
    }

    @Override
    public void schedule(RequestInfo requestInfo, Date date) {
        sendMessage(requestInfo, Message.DEFAULT_PRIORITY);
    }

    private void sendMessage(RequestInfo requestInfo, int priority) {
        if (connectionFactory == null && queue == null) {
            throw new IllegalStateException("ConnectionFactory and Queue cannot be null");
        }

        try (Connection queueConnection = connectionFactory.createConnection();
             Session queueSession = queueConnection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = queueSession.createProducer(queue)){

            producer.setPriority(priority);
            TextMessage message = queueSession.createTextMessage(String.valueOf(requestInfo.getId()));
            message.setJMSCorrelationID(requestInfo.getKey() + "_" + requestInfo.getId());
            message.setStringProperty("businessKey", requestInfo.getKey());
            queueConnection.start();
            producer.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Error when sending JMS message with executor job request", e);
        }
    }

    @Override
    public boolean accept(RequestInfo requestInfo, Date date) {
        return active && date == null;
    }

    @Override
    public void clear(Long requestId) {
        // not possible to clear
    }

    @Override
    public void cancel(RequestInfo requestInfo) {
        if(!active) {
            return;
        }
        try (Connection queueConnection = connectionFactory.createConnection();
                Session queueSession = queueConnection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);){

            MessageConsumer receiver = queueSession.createConsumer(queue, "JMSCorrelationID='" + requestInfo.getKey() + "_" + requestInfo.getId() + "'");
            Message message = receiver.receiveNoWait();
            if(message == null) {
                logger.info("Not possible to cancel job {} in jms queue {}", requestInfo.getId(), queue);
            }
        } catch(Exception e) {
            logger.info("Not possible to cancel job {} in jms queue {}", requestInfo.getId(), queue);
            logger.debug("JMS support executor failed due to {}", e.getMessage(), e);
        }
    }
}
