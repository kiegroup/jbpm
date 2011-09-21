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
package org.jbpm.formbuilder.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.easymock.EasyMock;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jbpm.formbuilder.server.form.FormDefDTO;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.FormItemDefDTO;
import org.jbpm.formbuilder.server.form.ListFormsDTO;
import org.jbpm.formbuilder.server.form.ListFormsItemsDTO;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormDefinitionService;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.form.FormServiceException;

public class RESTFormServiceTest extends RESTAbstractTest {

    //test happy path for RESTFormService.getForms(...)
    public void testGetFormsOK() throws Exception {
        List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
        forms.add(createMockForm("form1", "param1", "param2", "param3"));
        forms.add(createMockForm("form2", "paramA", "paramB", "paramC"));
        
        getFormsOK(forms);
    }

    //test happy path returning no forms for RESTFormService.getForms(...)
    public void testGetFormsOKEmpty() throws Exception {
        getFormsOK(new ArrayList<FormRepresentation>());
    }
    
    private void getFormsOK(List<FormRepresentation> retval) throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.expect(formService.getForms(EasyMock.same("somePackage"))).andReturn(retval).once();
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getForms("somePackage", context);
        EasyMock.verify(formService, context);
        
        Object entity = assertXmlOkResponse(resp);
        assertTrue("entity should be of type ListFormsDTO", entity instanceof ListFormsDTO);
        ListFormsDTO dto = (ListFormsDTO) entity;
        assertNotNull("dto.getForm() shouldn't be null", dto.getForm());
        assertEquals("dto.getForm() should be of size " + retval.size() + " but it is of size " + dto.getForm().size(), 
                dto.getForm().size(), retval.size());
    }
    
    //test response to a FormServiceException for RESTFormService.getForms(...)
    public void testGetFormsServiceProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        FormServiceException exception = new FormServiceException("Something going wrong");
        
        EasyMock.expect(formService.getForms(EasyMock.same("somePackage"))).andThrow(exception).once();
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getForms("somePackage", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test response to a FormEncodingException for RESTFormService.getForms(...)
    public void testGetFormsEncodingProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        FormEncodingException exception = new FormEncodingException("Something going wrong");
        
        FormRepresentationEncoder encoder = EasyMock.createMock(FormRepresentationEncoder.class);
        EasyMock.expect(encoder.encode(EasyMock.anyObject(FormRepresentation.class))).andThrow(exception).once();
        
        FormEncodingFactory.register(encoder, FormEncodingServerFactory.getDecoder());
        
        List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
        forms.add(createMockForm("form1", "param1", "param2", "param3"));
        forms.add(createMockForm("form2", "paramA", "paramB", "paramC"));
        
        EasyMock.expect(formService.getForms(EasyMock.same("somePackage"))).andReturn(forms).once();
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context, encoder);
        Response resp = restService.getForms("somePackage", context);
        EasyMock.verify(formService, context, encoder);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test happy path for RESTFormService.getForm(...)
    public void testGetFormOK() throws Exception {
        RESTFormService restService = new RESTFormService();
        
        FormRepresentation form = createMockForm("myForm", "myParam1", "myParam2", "myParam3");
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(formService.getForm(EasyMock.same("somePackage"), EasyMock.same("myFormId"))).andReturn(form);
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getForm("somePackage", "myFormId", context);
        EasyMock.verify(formService, context);
        
        Object entity = assertXmlOkResponse(resp);
        ListFormsDTO dto = (ListFormsDTO) entity;
        assertNotNull("dto.getForm() shouldn't be null", dto.getForm());
        assertEquals("dto.getForm() should be of one element but it is of size " + dto.getForm().size(), dto.getForm().size(), 1);
        FormDefDTO formDto = dto.getForm().iterator().next();
        assertTrue("formDto should be named myForm but it isn't", formDto.getJson().contains("myForm"));
        assertTrue("formDto should contain a parameter called myParam1 but it doesn't", formDto.getJson().contains("myParam1"));
        assertTrue("formDto should contain a parameter called myParam2 but it doesn't", formDto.getJson().contains("myParam2"));
        assertTrue("formDto should contain a parameter called myParam3 but it doesn't", formDto.getJson().contains("myParam3"));
    }
    
    //test response to a FormServiceException for RESTFormService.getForm(...)
    public void testGetFormServiceProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        FormServiceException exception = new FormServiceException("Something going wrong");
        
        EasyMock.expect(formService.getForm(EasyMock.same("somePackage"), EasyMock.same("myFormId"))).andThrow(exception).once();
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getForm("somePackage", "myFormId", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test response to a FormEncodingException for RESTFormService.getForm(...)
    public void testGetFormEncodingProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        FormEncodingException exception = new FormEncodingException("Something going wrong");
        
        FormRepresentationEncoder encoder = EasyMock.createMock(FormRepresentationEncoder.class);
        EasyMock.expect(encoder.encode(EasyMock.anyObject(FormRepresentation.class))).andThrow(exception).once();
        
        FormEncodingFactory.register(encoder, FormEncodingServerFactory.getDecoder());
        
        FormRepresentation form = createMockForm("form1", "param1", "param2", "param3");
        
        EasyMock.expect(formService.getForm(EasyMock.same("somePackage"), EasyMock.same("myFormId"))).andReturn(form).once();
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context, encoder);
        Response resp = restService.getForm("somePackage", "myFormId", context);
        EasyMock.verify(formService, context, encoder);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }

    //test happy path for RESTFormService.saveForm(...)
    public void testSaveFormOK() throws Exception {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormRepresentation form = RESTAbstractTest.createMockForm("formToBeSaved", "param1", "param2", "param3");
        EasyMock.expect(formService.saveForm(EasyMock.eq("somePackage"), EasyMock.eq(form))).andReturn("MY_FORM_ID").once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.saveForm(FormEncodingFactory.getEncoder().encode(form), "somePackage", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.CREATED);
        assertNotNull("resp.entity shouldn't be null", resp.getEntity());
        Object entity = resp.getEntity();
        assertNotNull("resp.metadata shouldn't be null", resp.getMetadata());
        Object contentType = resp.getMetadata().getFirst(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull("resp.entity shouldn't be null", contentType);
        assertEquals("contentType should be application/xml but is" + contentType, contentType, MediaType.APPLICATION_XML);
        String xml = entity.toString();
        String expected = "<formId>MY_FORM_ID</formId>";
        assertEquals("xml should be " + expected + " but it is " + xml, xml, expected);
    }

    //test response to a FormServiceException for RESTFormService.saveForm(...)
    public void testSaveFormServiceProblem() throws Exception {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormRepresentation form = RESTAbstractTest.createMockForm("formToBeSaved", "param1", "param2", "param3");
        FormServiceException exception = new FormServiceException("Something going wrong");
        EasyMock.expect(formService.saveForm(EasyMock.eq("somePackage"), EasyMock.eq(form))).andThrow(exception).once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.saveForm(FormEncodingFactory.getEncoder().encode(form), "somePackage", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test response to a FormEncodingException for RESTFormService.saveForm(...)
    public void testSaveFormEncodingProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormRepresentationDecoder decoder = EasyMock.createMock(FormRepresentationDecoder.class);
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), decoder);
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormRepresentation form = RESTAbstractTest.createMockForm("formToBeSaved", "param1", "param2", "param3");
        FormEncodingException exception = new FormEncodingException("Something going wrong");
        EasyMock.expect(decoder.decode(EasyMock.anyObject(String.class))).andThrow(exception).once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context, decoder);
        Response resp = restService.saveForm(FormEncodingFactory.getEncoder().encode(form), "somePackage", context);
        EasyMock.verify(formService, context, decoder);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }

    //test happy path for RESTFormService.deleteForm(...)
    public void testDeleteFormOK() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        formService.deleteForm(EasyMock.eq("somePackage"), EasyMock.eq("myFormId"));
        EasyMock.expectLastCall().once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.deleteForm("somePackage", "myFormId", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.OK);
    }
    
    //test response to a FormServiceException for RESTFormService.deleteForm(...)
    public void testDeleteFormServiceProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        formService.deleteForm(EasyMock.eq("somePackage"), EasyMock.eq("myFormId"));
        FormServiceException exception = new FormServiceException("Something going wrong");
        EasyMock.expectLastCall().andThrow(exception).once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.deleteForm("somePackage", "myFormId", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test happy path for RESTFormService.getFormItems(...)
    public void testGetFormItemsOK() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        Map<String, FormItemRepresentation> map = mockGetFormItems();
        EasyMock.expect(formService.getFormItems(EasyMock.eq("somePackage"))).andReturn(map).once();
        ServletContext context = EasyMock.createMock(ServletContext.class);
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getFormItems("somePackage", context);
        EasyMock.verify(formService, context);
        
        Object entity = assertXmlOkResponse(resp);
        assertTrue("entity should be of type ListFormsItemsDTO", entity instanceof ListFormsItemsDTO);
        ListFormsItemsDTO dto = (ListFormsItemsDTO) entity;
        List<FormItemDefDTO> items = dto.getFormItem();
        assertNotNull("items shouldn't be null", items);
        assertFalse("items shouldn't be empty", items.isEmpty());
        assertEquals("items size should be " + map.size() + " but is " + items.size(), items.size(), map.size());
    }
    
    //test response to a FormServiceException for RESTFormService.getFormItems(...)
    public void testGetFormItemsServiceProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormServiceException exception = new FormServiceException("Something going wrong");
        EasyMock.expect(formService.getFormItems(EasyMock.eq("somePackage"))).andThrow(exception).once();
        ServletContext context = EasyMock.createMock(ServletContext.class);
        restService.setFormService(formService);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getFormItems("somePackage", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test response to a FormEncodingException for RESTFormService.getFormItems(...)
    public void testGetFormItemsEncodingProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormRepresentationEncoder encoder = EasyMock.createMock(FormRepresentationEncoder.class);
        FormEncodingException exception = new FormEncodingException("Something going wrong");
        EasyMock.expect(encoder.encode(EasyMock.notNull(FormItemRepresentation.class))).andThrow(exception).anyTimes();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        Map<String, FormItemRepresentation> map = mockGetFormItems();
        EasyMock.expect(formService.getFormItems(EasyMock.eq("somePackage"))).andReturn(map).once();
        ServletContext context = EasyMock.createMock(ServletContext.class);
        restService.setFormService(formService);
        FormEncodingFactory.register(encoder, FormEncodingServerFactory.getDecoder());
        
        EasyMock.replay(formService, context, encoder);
        Response resp = restService.getFormItems("somePackage", context);
        EasyMock.verify(formService, context, encoder);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }

    private Map<String, FormItemRepresentation> mockGetFormItems() {
        Map<String, FormItemRepresentation> map = new HashMap<String, FormItemRepresentation>();
        FormRepresentation form = createMockForm("myForm", "param1", "param2", "param3");
        Iterator<FormItemRepresentation> myItems = form.getFormItems().iterator();
        map.put("name1", myItems.next());
        map.put("name2", myItems.next());
        map.put("name3", myItems.next());
        return map;
    }

    //test happy path for RESTFormService.getFormItem(...)
    public void testGetFormItemOK() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormItemRepresentation item = createMockForm("myForm", "param1").getFormItems().iterator().next();
        EasyMock.expect(formService.getFormItem(EasyMock.eq("somePackage"), EasyMock.eq("MY_FORM_ITEM_ID"))).andReturn(item).once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getFormItem("somePackage", "MY_FORM_ITEM_ID", context);
        EasyMock.verify(formService, context);
        
        Object entity = assertXmlOkResponse(resp);
        assertTrue("entity should be of type ListFormsItemsDTO", entity instanceof ListFormsItemsDTO);
        ListFormsItemsDTO dto = (ListFormsItemsDTO) entity;
        List<FormItemDefDTO> items = dto.getFormItem();
        assertNotNull("items shouldn't be null", items);
        assertFalse("items shouldn't be empty", items.isEmpty());
        assertEquals("items size should be 1 but is " + items.size(), items.size(), 1);
    }

    //test response to FormServiceException for RESTFormService.getFormItem(...)
    public void testGetFormItemServiceProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormServiceException exception = new FormServiceException("Something going wrong");
        EasyMock.expect(formService.getFormItem(EasyMock.eq("somePackage"), EasyMock.eq("MY_FORM_ITEM_ID"))).andThrow(exception).once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.getFormItem("somePackage", "MY_FORM_ITEM_ID", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }
    
    //test response to FormServiceException for RESTFormService.getFormItem(...)
    public void testGetFormItemEncodingProblem() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormRepresentationEncoder encoder = EasyMock.createMock(FormRepresentationEncoder.class);
        FormEncodingException exception = new FormEncodingException("Something going wrong");
        EasyMock.expect(encoder.encode(EasyMock.notNull(FormItemRepresentation.class))).andThrow(exception).anyTimes();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormItemRepresentation item = createMockForm("myForm", "param1").getFormItems().iterator().next();
        EasyMock.expect(formService.getFormItem(EasyMock.eq("somePackage"), EasyMock.eq("MY_ITEM_ID"))).andReturn(item).once();
        ServletContext context = EasyMock.createMock(ServletContext.class);
        restService.setFormService(formService);
        FormEncodingFactory.register(encoder, FormEncodingServerFactory.getDecoder());
        
        EasyMock.replay(formService, context, encoder);
        Response resp = restService.getFormItem("somePackage", "MY_ITEM_ID", context);
        EasyMock.verify(formService, context, encoder);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.INTERNAL_SERVER_ERROR);
    }

    //test happy path for RESTFormService.saveFormItem(...)
    public void testSaveFormItemOK() throws Exception {
        RESTFormService restService = new RESTFormService();
        FormDefinitionService formService = EasyMock.createMock(FormDefinitionService.class);
        FormItemRepresentation item = RESTAbstractTest.createMockForm("formToBeSaved", "param1").getFormItems().iterator().next();
        EasyMock.expect(formService.saveFormItem(EasyMock.eq("somePackage"), EasyMock.eq("MY_FORM_ITEM_ID"), EasyMock.eq(item))).
            andReturn("MY_FORM_ITEM_ID").once();
        restService.setFormService(formService);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        
        EasyMock.replay(formService, context);
        Response resp = restService.saveFormItem(FormEncodingFactory.getEncoder().encode(item), "somePackage", "MY_FORM_ITEM_ID", context);
        EasyMock.verify(formService, context);
        
        assertNotNull("resp shouldn't be null", resp);
        assertStatus(resp.getStatus(), Status.CREATED);
        assertNotNull("resp.entity shouldn't be null", resp.getEntity());
        Object entity = resp.getEntity();
        assertNotNull("resp.metadata shouldn't be null", resp.getMetadata());
        Object contentType = resp.getMetadata().getFirst(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull("resp.entity shouldn't be null", contentType);
        assertEquals("contentType should be application/xml but is" + contentType, contentType, MediaType.APPLICATION_XML);
        String xml = entity.toString();
        String expected = "<formItemId>MY_FORM_ITEM_ID</formItemId>";
        assertEquals("xml should be " + expected + " but it is " + xml, xml, expected);
    }

    public void testSaveFormItemServiceProblem() throws Exception {
        //TODO cause a FormServiceException
    }
    
    public void testSaveFormItemEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testDeleteFormItemOK() throws Exception {
        //TODO test happy path
    }
    
    public void testDeleteFormItemServiceProblem() throws Exception {
        //TODO cause a FormServiceException
    }

    public void testGetFormPreviewOK() throws Exception {
        //TODO test happy path
    }
    
    public void testGetFormPreviewEncodingProblem() throws Exception {
      //TODO cause a FormEncodingException
    }
    
    public void testGetFormPreviewTranslatorProblem() throws Exception {
      //TODO cause a LanguageException
    }
    
    public void testGetFormPreviewRendererProblem() throws Exception {
      //TODO cause a RendererException
    }
    
    public void testGetFormPreviewIOProblem() throws Exception {
      //TODO cause a IOException
    }
    
    public void testGetFormTemplateOK() throws Exception {
        //TODO test happy path
    }
    
    public void testGetFormTemplateEncodingProblem() throws Exception {
        //TODO cause a FormEncodingException
    }
    
    public void testGetFormTemplateTranslatorProblem() throws Exception {
        //TODO cause a LanguageException
    }
}
