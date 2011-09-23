/*
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
package org.jbpm.formbuilder.server.file;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.easymock.EasyMock;
import org.jbpm.formbuilder.server.FileException;
import org.jbpm.formbuilder.server.mock.MockAnswer;
import org.jbpm.formbuilder.server.mock.MockDeleteMethod;
import org.jbpm.formbuilder.server.mock.MockGetMethod;
import org.jbpm.formbuilder.server.mock.MockPostMethod;
import org.jbpm.formbuilder.server.mock.MockPutMethod;

public class GuvnorFileServiceTest extends TestCase {

    public void testStoreFileOK() throws Exception {
        GuvnorFileService service = createService("http://www.redhat.com", "user", "pass");
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, Integer> statuses = new HashMap<String, Integer>();
        statuses.put("GET http://www.redhat.com/rest/packages/somePackage/assets/fileName-upfile", 404);
        EasyMock.expect(client.executeMethod(EasyMock.isA(MockGetMethod.class))).
            andAnswer(new MockAnswer(statuses)).once();
        EasyMock.expect(client.executeMethod(EasyMock.isA(MockPostMethod.class))).andReturn(201).once();
        service.setClient(client);
        
        EasyMock.replay(client);
        String url = service.storeFile("somePackage", "fileName.txt", new byte[] { 1,2,3,4,5,6,7,8,9 } );
        EasyMock.verify(client);
        
        assertNotNull("url shouldn't be null", url);
    }
    
    public void testStoreFileProblem() throws Exception {
        GuvnorFileService service = createService("http://www.redhat.com", "user", "pass");
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, Integer> statuses = new HashMap<String, Integer>();
        statuses.put("GET http://www.redhat.com/rest/packages/somePackage/assets/fileName-upfile", 404);
        EasyMock.expect(client.executeMethod(EasyMock.isA(MockGetMethod.class))).
            andAnswer(new MockAnswer(statuses)).once();
        IOException exception = new IOException("mock io error");
        EasyMock.expect(client.executeMethod(EasyMock.isA(MockPostMethod.class))).andThrow(exception).once();
        service.setClient(client);
        
        EasyMock.replay(client);
        try {
            service.storeFile("somePackage", "fileName.txt", new byte[] { 1,2,3,4,5,6,7,8,9 } );
            fail("storeFile(...) should not succeed");
        } catch (FileException e) {
            assertNotNull("e shouldn't be null", e);
            Throwable cause = e.getCause();
            assertNotNull("cause shouldn't be null", cause);
            assertTrue("cause should be of type IOException", cause instanceof IOException);
        }
        EasyMock.verify(client);
    }
    
    public void testDeleteFileOK() throws Exception {
        //TODO implement
    }
    
    public void testDeleteFileIOProblem() throws Exception {
        //TODO implement
    }

    public void testLoadFilesByTypeOK() throws Exception {
        //TODO implement
    }
    
    public void testLoadFilesByTypeIOProblem() throws Exception {
        //TODO implement
    }
    
    public void testLoadFilesByTypeJAXBProblem() throws Exception {
        //TODO implement
    }

    public void testLoadFilesByTypeUnknownProblem() throws Exception {
        //TODO implement
    }
    
    public void testLoadFileOK() throws Exception {
        //TODO implement
    }
    
    public void testLoadFileIOProblem() throws Exception {
        //TODO implement
    }
    
    public void testLoadFileUnknownProblem() throws Exception {
        //TODO implement
    }

    private GuvnorFileService createService(String baseUrl, String user, String pass) {
        return new GuvnorFileService(baseUrl, user, pass) {
            @Override
            protected GetMethod createGetMethod(String url) {
                return new MockGetMethod(url);
            }
            @Override
            protected PostMethod createPostMethod(String url) {
                return new MockPostMethod(url);
            }
            @Override
            protected DeleteMethod createDeleteMethod(String url) {
                return new MockDeleteMethod(url);
            }
            @Override
            protected PutMethod createPutMethod(String url) {
                return new MockPutMethod(url);
            }
        };
    }
}
