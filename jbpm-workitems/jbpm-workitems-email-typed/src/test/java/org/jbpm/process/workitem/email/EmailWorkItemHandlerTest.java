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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.TypedWorkItemImpl;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.test.AbstractBaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.utils.ChainedProperties;
import org.kie.internal.utils.ClassLoaderUtil;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EmailWorkItemHandlerTest extends AbstractBaseTest {

    private Wiser wiser;

    private String emailHost;
    private String emailPort;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.jbpm.email.templates.dir", new File("src/test/resources/templates").getAbsolutePath());
        TemplateManager.reset();

        ChainedProperties props = ChainedProperties.getChainedProperties("email.conf", ClassLoaderUtil.getClassLoader(null, getClass(), false));
        emailHost = props.getProperty("mail.smtp.host", "localhost");
        emailPort = props.getProperty("mail.smtp.port", "2345");

        wiser = new Wiser();
        wiser.setHostname(emailHost);
        wiser.setPort(Integer.parseInt(emailPort));
        wiser.start();
        Thread.sleep(200);
    }

    @After
    public void tearDown() throws Exception {
        System.clearProperty("org.jbpm.email.templates.dir");
        if (wiser != null) {
            wiser.getMessages().clear();
            wiser.stop();
            wiser = null;
            Thread.sleep(1000);
        }
    }

    @Test
    public void testSingleTo() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, emailPort, null, null);

        SimpleMessage message = new SimpleMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        message.getRecipients().addRecipient(Recipient.to("person1@domain.com"));
        message.setFrom("person2@domain.com");
        message.setReplyTo("person3@domain.com");
        message.setSubject("Subject 1");
        message.setBody("Body 1");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);

        assertEquals(1, wiser.getMessages().size());

        MimeMessage msg = ((WiserMessage) wiser.getMessages().get(0)).getMimeMessage();
        // Side effect of MIME encoding (I think.. ): \r\n..
        String content = ((String) msg.getContent()).replace("\r\n", "");
        assertEquals(message.getBody(), content);
        assertEquals(message.getSubject(), msg.getSubject());
        assertEquals(message.getFrom(), ((InternetAddress) msg.getFrom()[0]).getAddress());
        assertEquals(message.getReplyTo(), ((InternetAddress) msg.getReplyTo()[0]).getAddress());
        assertEquals(message.getRecipients().getRecipients().get(0).getEmail(), ((InternetAddress) msg.getRecipients(RecipientType.TO)[0]).getAddress());
        assertNull(msg.getRecipients(RecipientType.CC));
        assertNull(msg.getRecipients(RecipientType.BCC));
    }

    @Test
    public void testSingleToWithSingleCCAndBCC() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, emailPort, null, null);

        SimpleMessage message = new SimpleMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        Recipients rcpts = message.getRecipients();
        rcpts.setRecipients(Arrays.asList(
                Recipient.to("person1@domain.com"),
                Recipient.cc("person2@domain.com"),
                Recipient.bcc("person3@domain.com")));
        message.setFrom("person4@domain.com");
        message.setReplyTo("person5@domain.com");
        message.setSubject("Subject 1");
        message.setBody("Body 1");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);

        assertEquals(3, wiser.getMessages().size());

        List<String> list = new ArrayList<String>(3);
        list.add(wiser.getMessages().get(0).getEnvelopeReceiver());
        list.add(wiser.getMessages().get(1).getEnvelopeReceiver());
        list.add(wiser.getMessages().get(2).getEnvelopeReceiver());

        assertTrue(list.contains("person1@domain.com"));
        assertTrue(list.contains("person2@domain.com"));
        assertTrue(list.contains("person3@domain.com"));

        for (int i = 0; i < wiser.getMessages().size(); ++i) {
            MimeMessage msg = ((WiserMessage) wiser.getMessages().get(i)).getMimeMessage();
            assertEquals(message.getFrom(), wiser.getMessages().get(i).getEnvelopeSender());
            String content = ((String) msg.getContent()).replace("\r\n", "");
            assertEquals(message.getBody(), content);
            assertEquals(message.getSubject(), msg.getSubject());
            assertEquals(message.getFrom(), ((InternetAddress) msg.getFrom()[0]).getAddress());
            assertEquals(message.getReplyTo(), ((InternetAddress) msg.getReplyTo()[0]).getAddress());
            List<Recipient> rr = message.getRecipients().getRecipients();
            assertEquals(getRecipientField(rr, "To").findFirst().orElse(null), ((InternetAddress) msg.getRecipients(RecipientType.TO)[0]).getAddress());
            assertEquals(getRecipientField(rr, "Cc").findFirst().orElse(null), ((InternetAddress) msg.getRecipients(RecipientType.CC)[0]).getAddress());
        }
    }

    private Stream<String> getRecipientField(List<Recipient> rr, String field) {
        return rr.stream().filter(r -> r.getType().equals(field)).map(Recipient::getEmail);
    }

    @Test
    public void testMultipleToWithSingleCCAndBCC() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, emailPort, null, null);

        SimpleMessage message = new SimpleMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        message.getRecipients().setRecipients(Arrays.asList(
                Recipient.to("person1@domain.com"),
                Recipient.to("person2@domain.com"),
                Recipient.cc("person3@domain.com"),
                Recipient.cc("person4@domain.com"),
                Recipient.bcc("person5@domain.com"),
                Recipient.bcc("person6@domain.com")
        ));

        message.setFrom("person4@domain.com");
        message.setReplyTo("person5@domain.com");
        message.setSubject("Subject 1");
        message.setBody("Body 1");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);

        assertEquals(6, wiser.getMessages().size());

        List<String> list = new ArrayList<String>(6);
        for (int i = 0; i < 6; ++i) {
            list.add(wiser.getMessages().get(i).getEnvelopeReceiver());
        }

        for (int i = 1; i < 7; ++i) {
            assertTrue(list.contains("person" + i + "@domain.com"));
        }

        // We know from previous test that all MimeMessages will be identical
        MimeMessage msg = ((WiserMessage) wiser.getMessages().get(0)).getMimeMessage();
        assertEquals(message.getFrom(), wiser.getMessages().get(0).getEnvelopeSender());
        String content = ((String) msg.getContent()).replace("\r\n", "");
        assertEquals(message.getBody(), content);
        assertEquals(message.getSubject(), msg.getSubject());
        assertEquals(message.getFrom(), ((InternetAddress) msg.getFrom()[0]).getAddress());
        assertEquals(message.getReplyTo(), ((InternetAddress) msg.getReplyTo()[0]).getAddress());
        assertEquals(getRecipientField(message.getRecipients().getRecipients(), "To").collect(joining("; ")),
                     ((InternetAddress) msg.getRecipients(RecipientType.TO)[0]).getAddress() + "; " + ((InternetAddress) msg.getRecipients(RecipientType.TO)[1]).getAddress());
        assertEquals(getRecipientField(message.getRecipients().getRecipients(), "Cc").collect(joining("; ")),
                     ((InternetAddress) msg.getRecipients(RecipientType.CC)[0]).getAddress() + "; " + ((InternetAddress) msg.getRecipients(RecipientType.CC)[1]).getAddress());
    }

    @Test(expected = WorkItemHandlerRuntimeException.class)
    public void testFailedExecuteToHandleException() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, "123", null, null);

        SimpleMessage message = new SimpleMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        message.getRecipients().addRecipient(Recipient.to("person1@domain.com"));
        message.setFrom("person2@domain.com");
        message.setReplyTo("person3@domain.com");
        message.setSubject("Subject 1");
        message.setSubject("Body 1");

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);
    }

    @Test
    public void testEmailWithTemplate() throws Exception {
        EmailWorkItemHandler handler = new EmailWorkItemHandler();
        handler.setConnection(emailHost, emailPort, null, null);



        TemplatedMessage message = new TemplatedMessage();
        TypedWorkItemImpl<Message, Object> workItem = new TypedWorkItemImpl<>(message);
        message.getRecipients().addRecipient(Recipient.to("person1@domain.com"));
        message.setFrom("person2@domain.com");
        message.setReplyTo("person3@domain.com");
        message.setSubject("Subject 1");


        Map<String, Object> params = new HashMap<>();
        params.put("Name", "John Doe");

        message.setTemplateManager(TemplateManager.get());
        message.setTemplate("basic-email");
        message.setTemplateParameters(params);


        String expectedBody = "<html><body>Hello John Doe</body></html>";

        WorkItemManager manager = new DefaultWorkItemManager(null);
        handler.executeWorkItem(workItem, manager);

        assertEquals(1, wiser.getMessages().size());

        MimeMessage msg = ((WiserMessage) wiser.getMessages().get(0)).getMimeMessage();
        // Side effect of MIME encoding (I think.. ): \r\n..
        String content = ((String) msg.getContent()).replace("\r\n", "");
        assertEquals(expectedBody, content);
        assertEquals(message.getSubject(), msg.getSubject());
        assertEquals(message.getFrom(), ((InternetAddress) msg.getFrom()[0]).getAddress());
        assertEquals(message.getReplyTo(), ((InternetAddress) msg.getReplyTo()[0]).getAddress());
        assertEquals(message.getRecipients().getRecipients().get(0).getEmail(),
                     ((InternetAddress) msg.getRecipients(RecipientType.TO)[0]).getAddress());
        assertNull(msg.getRecipients(RecipientType.CC));
        assertNull(msg.getRecipients(RecipientType.BCC));
    }
}

