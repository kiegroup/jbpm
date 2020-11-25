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
package org.jbpm.services.task;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jbpm.process.instance.impl.util.LoggingPrintStream;
import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.jbpm.services.task.utils.ContentMarshallerHelper;
import org.jbpm.services.task.utils.MVELUtils;
import org.jbpm.test.listener.task.CountDownTaskEventListener;
import org.jbpm.test.persistence.util.PersistenceUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.model.Content;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class HumanTaskServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(HumanTaskServicesBaseTest.class);

    protected InternalTaskService taskService;

    @BeforeClass
    public static void configure() {
        LoggingPrintStream.interceptSysOutSysErr();
    }

    @AfterClass
    public static void reset() {
        LoggingPrintStream.resetInterceptSysOutSysErr();
    }

    public void tearDown() {
        if( taskService != null ) {
            int removeAllTasks = taskService.removeAllTasks();
            logger.debug("Number of tasks removed {}", removeAllTasks);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map fillUsersOrGroups(String mvelFileName) throws Exception {
        Map<String, Object> vars = new HashMap<String, Object>();
        Reader reader = null;
        Map<String, Object> result = null;

        try {
            reader = new InputStreamReader(HumanTaskServicesBaseTest.class.getResourceAsStream(mvelFileName));
            result = (Map<String, Object>) MVELUtils.eval(reader, vars);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return result;
    }

    protected final static String mySubject = "My Subject";
    protected final static String myBody = "My Body";

    protected static Map<String, String> fillMarshalSubjectAndBodyParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("subject", mySubject);
        params.put("body", myBody);
        return params;
    }

    protected static void checkContentSubjectAndBody(Object unmarshalledObject) {
        assertTrue("Content is null.", unmarshalledObject != null && unmarshalledObject.toString() != null);
        String content = unmarshalledObject.toString();
        boolean match = false;
        if (("{body=" + myBody + ", subject=" + mySubject + "}").equals(content)
                || ("{subject=" + mySubject + ", body=" + myBody + "}").equals(content)) {
            match = true;
        }
        assertTrue("Content does not match.", match);
    }

    protected void printTestName() {
        logger.info("Running {}.{} ", this.getClass().getSimpleName(), Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Creates date using default format - "yyyy-MM-dd"
     */
    protected Date createDate(String dateString) {
        return createDate(dateString, "yyyy-MM-dd");
    }

    protected Date createDate(String dateString, String dateFormat) {
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
        try {
            return fmt.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Can't create date from string '" + dateString + "' using '" + dateFormat + "' format!", e);
        }
    }

    protected JaxbContent xmlRoundTripContent(Content content) {
        JaxbContent xmlContent = new JaxbContent(content);
        JaxbContent xmlCopy = null;
        try {
            Marshaller marshaller = JAXBContext.newInstance(JaxbContent.class).createMarshaller();

            // marshal
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(xmlContent, stringWriter);

            // unmarshal
            Unmarshaller unmarshaller = JAXBContext.newInstance(JaxbContent.class).createUnmarshaller();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
            xmlCopy = (JaxbContent) unmarshaller.unmarshal(inputStream);

            for(Field field : JaxbContent.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object orig = field.get(xmlContent);
                Object roundTrip = field.get(xmlCopy);
                if( orig instanceof byte[] ) {
                    Assert.assertTrue(Arrays.equals((byte[]) orig, (byte[]) roundTrip));
                } else {
                    Assert.assertEquals(field.getName(), orig, roundTrip);
                }
            }
        } catch(Exception e) {
            logger.error("Unable to complete round trip: " + e.getMessage(), e );
            Assert.fail("Unable to complete round trip: " + e.getMessage());
        }

        Object orig = ContentMarshallerHelper.unmarshall(content.getContent(), null);
        assertNotNull( "Round tripped JaxbContent is null!", xmlCopy );
        Object roundTrip = ContentMarshallerHelper.unmarshall(xmlCopy.getContent(), null);
        Assert.assertEquals(orig, roundTrip);

        return xmlCopy;
    }

    protected static PoolingDataSourceWrapper setupPoolingDataSource() {
        return PersistenceUtil.setupPoolingDataSource("jdbc/jbpm-ds");
    }

    protected void addCountDownListener(CountDownTaskEventListener countDownListener) {
        if (taskService instanceof EventService) {
            ((EventService<TaskLifeCycleEventListener>) taskService).registerTaskEventListener(countDownListener);
        }
    }

    protected void removeCountDownListener(CountDownTaskEventListener countDownListener) {
        if (taskService instanceof EventService) {
            ((EventService<TaskLifeCycleEventListener>) taskService).removeTaskEventListener(countDownListener);
        }
    }
}
