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
package org.jbpm.formbuilder.server.form;

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
import org.easymock.IAnswer;
import org.jbpm.formbuilder.server.RESTAbstractTest;
import org.jbpm.formbuilder.server.mock.MockDeleteMethod;
import org.jbpm.formbuilder.server.mock.MockGetMethod;
import org.jbpm.formbuilder.server.mock.MockPostMethod;
import org.jbpm.formbuilder.server.mock.MockPutMethod;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;

public class GuvnorFormDefinitionServiceTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
    }
    
    //test happy path for insert for GuvnorFormDefinitionService.saveForm(...)
    public void testSaveFormOK() throws Exception {
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form1AutoForm.formdef", "{}");
        responses.put("POST http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form1AutoForm.formdef", "OK");
        EasyMock.expect(client.executeMethod(EasyMock.anyObject(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new IllegalArgumentException("unexpected call"))).anyTimes();
        GuvnorFormDefinitionService service = createService("http://www.redhat.com", "", "");
        service.setClient(client);
        FormRepresentation form = RESTAbstractTest.createMockForm("form1", "oneParam");
        
        EasyMock.replay(client);
        String formId = service.saveForm("somePackage", form);
        EasyMock.verify(client);
        
        assertNotNull("formId shouldn't be null", formId);
    }
    
    //test happy path for update for GuvnorFormDefinitionService.saveForm(...)
    public void testSaveFormUpdateOK() throws Exception {
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        FormRepresentation form = RESTAbstractTest.createMockForm("form2", "oneParam");
        String jsonForm = FormEncodingFactory.getEncoder().encode(form);
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form2AutoForm.formdef", jsonForm);
        responses.put("PUT http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form2AutoForm.formdef", "OK");
        EasyMock.expect(client.executeMethod(EasyMock.anyObject(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new IllegalArgumentException("unexpected call"))).anyTimes();
        GuvnorFormDefinitionService service = createService("http://www.redhat.com", "", "");
        service.setClient(client);
        
        EasyMock.replay(client);
        String formId = service.saveForm("somePackage", form);
        EasyMock.verify(client);
        
        assertNotNull("formId shouldn't be null", formId);
    }
    
    //test response to a FormEncodingException for GuvnorFormDefinitionService.saveForm(...)
    public void testSaveFormEncodingProblem() throws Exception {
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        FormRepresentation form = RESTAbstractTest.createMockForm("form2", "oneParam");
        String jsonForm = FormEncodingFactory.getEncoder().encode(form);
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form2AutoForm.formdef", jsonForm);
        EasyMock.expect(client.executeMethod(EasyMock.anyObject(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new IllegalArgumentException("unexpected call"))).anyTimes();
        GuvnorFormDefinitionService service = createService("http://www.redhat.com", "", "");
        service.setClient(client);
        FormRepresentationDecoder decoder = EasyMock.createMock(FormRepresentationDecoder.class);
        EasyMock.expect(decoder.decode(EasyMock.eq(jsonForm))).andThrow(new FormEncodingException("Something going wrong")).once();
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), decoder);
        
        EasyMock.replay(client, decoder);
        try {
            service.saveForm("somePackage", form);
            fail("saveForm(...) Shouldn't succeed");
        } catch (FormServiceException e) {
            assertNotNull("e shouldn't be null", e);
            Throwable cause = e.getCause();
            assertNotNull("cause shouldn't be null", cause);
            assertTrue("cause should be of type FormEncodingException", cause instanceof FormEncodingException);
        }
        EasyMock.verify(client, decoder);
    }
    
    //test response to a IOException for GuvnorFormDefinitionService.saveForm(...)
    public void testSaveFormIOProblem() throws Exception {
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        FormRepresentation form = RESTAbstractTest.createMockForm("form2", "oneParam");
        String jsonForm = FormEncodingFactory.getEncoder().encode(form);
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form2AutoForm.formdef", jsonForm);
        EasyMock.expect(client.executeMethod(EasyMock.anyObject(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new IOException("MOCKING IO ERROR"))).anyTimes();
        GuvnorFormDefinitionService service = createService("http://www.redhat.com", "", "");
        service.setClient(client);
        
        EasyMock.replay(client);
        try {
            service.saveForm("somePackage", form);
            fail("saveForm(...) Shouldn't succeed");
        } catch (FormServiceException e) {
            assertNotNull("e shouldn't be null", e);
            Throwable cause = e.getCause();
            assertNotNull("cause shouldn't be null", cause);
            assertTrue("cause should be of type IOException", cause instanceof IOException);
        }
        EasyMock.verify(client);
    }
    
    //test response to a NullPointerException for GuvnorFormDefinitionService.saveForm(...)
    public void testSaveFormUnknownProblem() throws Exception {
        HttpClient client = EasyMock.createMock(HttpClient.class);
        Map<String, String> responses = new HashMap<String, String>();
        FormRepresentation form = RESTAbstractTest.createMockForm("form2", "oneParam");
        String jsonForm = FormEncodingFactory.getEncoder().encode(form);
        responses.put("GET http://www.redhat.com/org.drools.guvnor.Guvnor/api/packages/somePackage/form2AutoForm.formdef", jsonForm);
        EasyMock.expect(client.executeMethod(EasyMock.anyObject(MockGetMethod.class))).
            andAnswer(new MockAnswer(responses, new NullPointerException("MOCKING IO ERROR"))).anyTimes();
        GuvnorFormDefinitionService service = createService("http://www.redhat.com", "", "");
        service.setClient(client);
        
        EasyMock.replay(client);
        try {
            service.saveForm("somePackage", form);
            fail("saveForm(...) Shouldn't succeed");
        } catch (FormServiceException e) {
            assertNotNull("e shouldn't be null", e);
            Throwable cause = e.getCause();
            assertNotNull("cause shouldn't be null", cause);
            assertTrue("cause should be of type NullPointerException", cause instanceof NullPointerException);
        }
        EasyMock.verify(client);
    }

    public void testSaveFormItemOK() throws Exception {
        //TODO happy path
    }
    
    public void testSaveFormItemEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testSaveFormItemIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testSaveFormItemUnkownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetFormOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetFormEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testGetFormIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetFormUnkownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetFormByUUIDOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetFormByUUIDEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testGetFormByUUIDIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetFormByUUIDJAXBProblem() throws Exception {
        //TODO cause a JAXBException
    }

    public void testGetFormByUUIDUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetFormItemOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetFormItemEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testGetFormItemIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetFormItemUnkownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testGetFormItemsOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetFormItemServiceProblem() throws Exception {
        //TODO cause a FormServiceException
    }
    
    public void testGetFormItemsIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetFormItemsUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }

    public void testGetFormsOK() throws Exception {
        //TODO happy path
    }
    
    public void testGetFormsIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testGetFormsServiceProblem() throws Exception {
        //TODO cause a FormServiceException
    }
    
    public void testGetFormsUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }
    
    public void testDeleteFormOK() throws Exception {
        //TODO happy path
    }
    
    public void testDeleteFormIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testDeleteFormUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }

    public void testDeleteFormItemOK() throws Exception {
        //TODO happy path
    }
    
    public void testDeleteFormItemIOProblem() throws Exception {
        //TODO cause a IOException
    }
    
    public void testDeleteFormItemUnknownProblem() throws Exception {
        //TODO cause a NullPointerException
    }

    public void testSaveTemplateOK() throws Exception {
        //TODO happy path
    }

    public void testSaveTemplateGetProblem() throws Exception {
        //TODO cause a IOException on templateExists
    }
    
    public void testSaveTemplatePostProblem() throws Exception {
        //TODO cause a IOException on ensureTemlpateAsset
    }
    
    public void testSaveTemplatePutProblem() throws Exception {
        //TODO cause a IOException on executeMethod
    }
    
    protected class MockAnswer implements IAnswer<Integer> {

        private final Map<String, String> responses;
        private final Throwable exception;
        
        public MockAnswer(Map<String, String> responses, Throwable exception) {
            this.responses = responses;
            this.exception = exception;
        }
        
        @Override
        public Integer answer() throws Throwable {
            Object[] params = EasyMock.getCurrentArguments();
            if (params[0] instanceof MockGetMethod) {
                MockGetMethod method = (MockGetMethod) params[0];
                String key = "GET " + method.getURI().toString();
                if (responses.containsKey(key)) {
                    method.setResponseBodyAsString(responses.get(key));
                } else if (exception != null) throw exception;
                return 200;
            } else if (params[0] instanceof MockPostMethod) {
                MockPostMethod method = (MockPostMethod) params[0];
                String key = "POST " + method.getURI().toString();
                if (responses.containsKey(key)) {
                    method.setResponseBodyAsString(responses.get(key));
                } else if (exception != null) throw exception;
                return 201;
            } else if (params[0] instanceof MockPutMethod) {
                MockPutMethod method = (MockPutMethod) params[0];
                String key = "PUT " + method.getURI().toString();
                if (responses.containsKey(key)) {
                    method.setResponseBodyAsString(responses.get(key));
                } else if (exception != null) throw exception;
                return 201;
            } else if (params[0] instanceof MockDeleteMethod) {
                MockDeleteMethod method = (MockDeleteMethod) params[0];
                String key = "PUT " + method.getURI().toString();
                if (responses.containsKey(key)) {
                    method.setResponseBodyAsString(responses.get(key));
                } else if (exception != null) throw exception;
                return 201;
            } else {
                throw new IllegalArgumentException("params[0] shouldn't be of type " + params[0]);
            }
        }
    }
    
    private GuvnorFormDefinitionService createService(String baseUrl, String user, String pass) {
        return new GuvnorFormDefinitionService(baseUrl, user, pass) {
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
