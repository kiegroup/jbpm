/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.workitem.email;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.TypedWorkItemImpl;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.test.AbstractBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.TypedWorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.MultipleAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.PlainAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SendHtmlTest extends AbstractBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SendHtmlTest.class);
    private Wiser wiser;

    private String emailHost;
    private String emailPort;

    private static String authUsername = "cpark";
    private static String authPassword = "yourbehindwhat?";

    private Random random = new Random();
    private int uniqueTestNum = -1;

    private EmailWorkItemHandler emailWorkItemHandler = new EmailWorkItemHandler();

    @Before
    public void setUp() throws Exception {
        uniqueTestNum = random.nextInt(Integer.MAX_VALUE);

        emailHost = "localhost";
        int emailPortInt;
        do {
            emailPortInt = random.nextInt((2 * Short.MAX_VALUE - 1));
        } while (emailPortInt < 4096);

        emailPort = Integer.toString(emailPortInt);

        wiser = new Wiser(Integer.parseInt(emailPort));
        wiser.start();
    }

    @After
    public void tearDown() throws Exception {
        if (wiser != null) {
            wiser.getMessages().clear();
            wiser.stop();
            wiser = null;
        }
    }

    @SuppressWarnings("unused")
    private class ExtendedConnection extends Connection {

        private String extraField;
    }

    @Test
    public void testConnectionEquals() {
        Connection connA = new Connection();
        Connection connB = new Connection();

        // null test
        assertTrue(!connA.equals(null));
        // different class test
        assertTrue(!connA.equals("og"));
        // extended class test
        ExtendedConnection connExt = new ExtendedConnection();
        assertTrue(!connA.equals(connExt));
        // null fields test
        assertTrue(connA.equals(connB));

        // all null vs filled field test
        connA.setHost("Human");
        connA.setPort("Skin");
        connA.setUserName("Viral");
        connA.setPassword("Protein Gate");
        assertTrue(!connA.equals(connB));

        // filled field test
        connB.setHost(connA.getHost());
        connB.setPort(new String(connA.getPort()));
        connB.setUserName(connA.getUserName());
        connB.setPassword(connA.getPassword());
        assertTrue(connA.equals(connB));

        // some null vs filled field test
        connA.setPassword(null);
        connB.setPassword(null);
        assertTrue(connA.equals(connB));

        // boolean
        connA.setStartTls(true);
        assertTrue(!connA.equals(connB));
        connB.setStartTls(true);
        assertTrue(connA.equals(connB));
        connB.setStartTls(false);
        assertTrue(!connA.equals(connB));
    }

    @Test
    public void verifyWiserServerWorks() throws Exception {
        // Input
        String testMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Recipient toAddress = Recipient.to("boyd@crowdergang.org");
        String fromAddress = "rgivens@kty.us.gov";

        // Setup email
        TypedWorkItem<Message, Object> workItem = createEmailWorkItem(toAddress, fromAddress, testMethodName);
        Connection connection = new Connection(emailHost, emailPort);

        sendAndCheckThatMessagesAreSent(workItem, connection);
    }

    @Test
    public void sendHtmlWithAuthentication() throws Exception {
        // Add authentication to Wiser SMTP server
        wiser.getServer().setAuthenticationHandlerFactory(new TestAuthHandlerFactory());

        // Input
        String testMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Recipient toAddress = Recipient.to("rgivens@kty.us.gov");
        String fromAddress = "whawkins@kty.us.gov";

        // Setup email
        TypedWorkItem<Message, Object> workItem = createEmailWorkItem(toAddress, fromAddress, testMethodName);
        Connection connection = new Connection(emailHost, emailPort, authUsername, authPassword);

        sendAndCheckThatMessagesAreSent(workItem, connection);
    }

    @Test
    public void sendHtmlWithAuthenticationAndAttachments() throws Exception {
        // Add authentication to Wiser SMTP server
        wiser.getServer().setAuthenticationHandlerFactory(new TestAuthHandlerFactory());

        // Input
        String testMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Recipient toAddress = Recipient.to("rgivens@kty.us.gov");
        String fromAddress = "whawkins@kty.us.gov";

        // Setup email
        TypedWorkItem<Message, Object> workItem = createEmailWorkItemWithAttachment(toAddress, fromAddress, testMethodName);
        Connection connection = new Connection(emailHost, emailPort, authUsername, authPassword);

        // send email
        Email email = emailWorkItemHandler.createEmail(workItem, connection);
        SendHtml.sendHtml(email, connection);

        List<WiserMessage> messages = wiser.getMessages();
        assertEquals(1, messages.size());

        MimeMessage message = messages.get(0).getMimeMessage();
        assertEquals(workItem.getParameters().getSubject(), message.getSubject());
        assertTrue(Arrays.equals(InternetAddress.parse(workItem.getParameters().getRecipients().getRecipients().get(0).getEmail()),
                                 message.getRecipients(RecipientType.TO)));

        assertTrue(message.getContent() instanceof Multipart);

        Multipart multipart = (Multipart) message.getContent();
        assertEquals(2, multipart.getCount());
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                continue; // dealing with attachments only
            }

            assertEquals("email.gif", bodyPart.getFileName());
        }
    }

    @Test
    public void sendHtmlWithBadAuthentication() throws Exception {
        // Add authentication to Wiser SMTP server
        wiser.getServer().setAuthenticationHandlerFactory(new TestAuthHandlerFactory());

        // Input
        String testMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Recipient recipient = Recipient.to("mags@bennetstore.com");
        String fromAddress = "rgivens@kty.us.gov";

        checkBadAuthentication(recipient, fromAddress, testMethodName, authUsername, "bad password");
        checkBadAuthentication(recipient, fromAddress, testMethodName, "badUserName", authPassword);
    }

    @Test
    public void useEmailWorkItemHandlerWithAuthentication() throws Exception {
        // Add authentication to Wiser SMTP server
        wiser.getServer().setAuthenticationHandlerFactory(new TestAuthHandlerFactory());

        // Input
        String testMethodName = Thread.currentThread().getStackTrace()[1].getMethodName();
        Recipient toAddress = Recipient.to("rgivens@yahoo.com");
        String fromAddress = "rgivens@kty.us.gov";

        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, emailPort, authUsername, authPassword);

        SimpleMessage message = new SimpleMessage();
        TypedWorkItem<Message, Object> workItem = new TypedWorkItemImpl<>(message);

        message.getRecipients().addRecipient(toAddress);
        message.setFrom(fromAddress);
        message.setReplyTo(fromAddress);
        message.setSubject("Test mail for " + testMethodName);
        message.setBody("Don't forget to check on Boyd later today.");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);

        List<WiserMessage> messages = wiser.getMessages();
        assertEquals(1, messages.size());

        for (WiserMessage wiserMessage : messages) {
            MimeMessage mimeMessage = wiserMessage.getMimeMessage();
            assertEquals(workItem.getParameters().getSubject(), mimeMessage.getSubject());
            assertTrue(Arrays.equals(InternetAddress.parse(toAddress.getEmail()),
                                     mimeMessage.getRecipients(RecipientType.TO)));
        }
    }

    /**
     * Helper methods
     */
    private void sendAndCheckThatMessagesAreSent(TypedWorkItem<Message, Object> workItem, Connection connection) throws Exception {
        // send email
        Email email = emailWorkItemHandler.createEmail(workItem, connection);
        SendHtml.sendHtml(email, connection);

        List<WiserMessage> messages = wiser.getMessages();
        assertEquals(1, messages.size());

        for (WiserMessage wiserMessage : messages) {
            MimeMessage message = wiserMessage.getMimeMessage();
            assertEquals(workItem.getParameters().getSubject(), message.getSubject());
            assertTrue(Arrays.equals(InternetAddress.parse(workItem.getParameters().getRecipients().getRecipients().get(0).getEmail()),
                                     message.getRecipients(RecipientType.TO)));
        }
    }

    private void checkBadAuthentication(Recipient toAddress, String fromAddress, String testMethodName,
                                        String username, String password) {
        // Setup email
        TypedWorkItem<Message, Object> workItem = createEmailWorkItem(toAddress, fromAddress, testMethodName);
        Connection connection = new Connection(emailHost, emailPort, username, password);

        // send email
        Email email = emailWorkItemHandler.createEmail(workItem, connection);
        try {
            SendHtml.sendHtml(email, connection);
        } catch (Throwable t) {
            assertTrue("Unexpected exception of type " + t.getClass().getSimpleName()
                               + ", not " + t.getClass().getSimpleName(), (t instanceof RuntimeException));
            assertNotNull("Expected RuntimeException to have a cause.", t.getCause());
            Throwable cause = t.getCause();
            assertNotNull("Expected cause to have a cause.", cause.getCause());
            cause = cause.getCause();
            assertTrue("Unexpected exception of type " + cause.getClass().getSimpleName()
                               + ", not " + cause.getClass().getSimpleName(), (cause instanceof AuthenticationFailedException));
        }
    }

    private TypedWorkItem<Message, Object> createEmailWorkItem(Recipient toAddress, String fromAddress, String testMethodName) {
        SimpleMessage message = new SimpleMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        message.getRecipients().addRecipient(toAddress);
        message.setFrom(fromAddress);
        message.setReplyTo(fromAddress);

        String subject = this.getClass().getSimpleName() + " test message [" + uniqueTestNum + "]";
        String body = "\nThis is the test message generated by the " + testMethodName + " test (" + uniqueTestNum + ").\n";
        message.setSubject(subject);
        message.setBody(body);

        return workItem;
    }

    private TypedWorkItem<Message, Object> createEmailWorkItemWithAttachment(Recipient toAddress, String fromAddress, String testMethodName) {
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>();
        SimpleMessage message = new SimpleMessage();
        workItem.setParameters(message);

        message.getRecipients().addRecipient(toAddress);

        message.setFrom(fromAddress);
        message.setReplyTo(fromAddress);

        String subject = this.getClass().getSimpleName() + " test message [" + uniqueTestNum + "]";
        String body = "\nThis is the test message generated by the " + testMethodName + " test (" + uniqueTestNum + ").\n";

        message.setSubject(subject);
        message.setBody(body);

        message.setAttachments(Collections.singletonList("classpath:/icons/email.gif"));

        return workItem;
    }

    private static class TestAuthHandlerFactory implements AuthenticationHandlerFactory {

        MultipleAuthenticationHandlerFactory authHandleFactory = new MultipleAuthenticationHandlerFactory();

        public TestAuthHandlerFactory() {
            UsernamePasswordValidator validator = new UsernamePasswordValidator() {
                public void login(String username, String password) throws LoginFailedException {
                    if (!authUsername.equals(username) || !authPassword.equals(password)) {
                        logger.debug("Tried to login with user/password [{}/{}]", username, password);
                        throw new LoginFailedException("Incorrect password for user " + authUsername);
                    }
                }
            };
            authHandleFactory.addFactory(new LoginAuthenticationHandlerFactory(validator));
            authHandleFactory.addFactory(new PlainAuthenticationHandlerFactory(validator));
        }

        public AuthenticationHandler create() {
            return authHandleFactory.create();
        }

        public List<String> getAuthenticationMechanisms() {
            return authHandleFactory.getAuthenticationMechanisms();
        }
    }
}

