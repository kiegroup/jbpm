/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.rest;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.process.workitem.core.TestWorkItemManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static org.jbpm.process.workitem.rest.RESTWorkItemHandler.PARAM_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.stop.Stop.stopQuietly;
import static org.mockserver.verify.VerificationTimes.exactly;


public class RESTWorkItemHandlerProxyTest {

    private static ClientAndServer proxy;
    private static ClientAndServer mockServer;
    private static int serverPort;

    @BeforeClass
    public static void startProxy() {
        //needed mainly for ibm jdk 1.8
        ConfigurationProperties.useBouncyCastleForKeyAndCertificateGeneration(true);

        proxy = ClientAndServer.startClientAndServer();
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", Integer.toString(proxy.getLocalPort()));
        mockServer = startClientAndServer();
        serverPort = mockServer.getLocalPort();
    }

    @AfterClass
    public static void stopProxy() {
        stopQuietly(mockServer);
        stopQuietly(proxy);
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
    }

    @Before
    public void startMockServer() {
        mockServer.reset();
        proxy.reset();
    }

    @Test
    public void testGETWithProxyOperation() {
        System.setProperty(RESTWorkItemHandler.USE_SYSTEM_PROPERTIES, "true");
        try {
            invokeGet();
            proxy.verify(
                         request()
                                  .withPath("/test"),
                         exactly(1));

        } finally {
            System.clearProperty(RESTWorkItemHandler.USE_SYSTEM_PROPERTIES);
        }
    }
    
  
    @Test
    public void testGETWithoutProxyOperation() {
        invokeGet();
        proxy.verify(
                     request()
                              .withPath("/test"),
                     exactly(0));
    }
    
    @Test
    public void testGETWithProxyOperationLegacy() {
        System.setProperty(RESTWorkItemHandler.USE_SYSTEM_PROPERTIES, "true");
        RESTWorkItemHandler.HTTP_CLIENT_API_43=false;
        try {
            invokeGet();
            proxy.verify(
                         request()
                                  .withPath("/test"),
                         exactly(1));

        } finally {
            System.clearProperty(RESTWorkItemHandler.USE_SYSTEM_PROPERTIES);
            RESTWorkItemHandler.HTTP_CLIENT_API_43=true;
        }
    }
    
    @Test
    public void testGETWithoutProxyOperationLegacy() {
        RESTWorkItemHandler.HTTP_CLIENT_API_43=false;
        try {
            invokeGet();
            proxy.verify(
                         request()
                                  .withPath("/test"),
                         exactly(0));

        } finally {
            RESTWorkItemHandler.HTTP_CLIENT_API_43=true;
        }
    }

    


    private void invokeGet() {
        // given
        mockServer
                  .when(
                        request().withPath("/test"))
                  .respond(
                           response()
                                     .withHeaders(
                                                  new Header(CONTENT_TYPE.toString(), "plain/test"))
                                     .withBody("Hello from REST"));

        WorkItemImpl workItem = new WorkItemImpl();
        workItem.setParameter("Url", "http://localhost:" + serverPort + "/test");
        workItem.setParameter("Method", "GET");
        TestWorkItemManager manager = new TestWorkItemManager();
        new RESTWorkItemHandler().executeWorkItem(workItem, manager);
        Map<String, Object> results = ((TestWorkItemManager) manager).getResults(workItem.getId());
        assertNotNull("results cannot be null",
                      results);

        String result = (String) results.get(PARAM_RESULT);
        assertNotNull("result cannot be null",
                      result);
        assertEquals("Hello from REST",
                     result);
    }

}
