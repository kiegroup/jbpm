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
package org.jbpm.formbuilder.server.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.jbpm.formbuilder.server.GuvnorHelper;
import org.jbpm.formbuilder.server.RESTAbstractTest;
import org.jbpm.formbuilder.server.form.FormDefDTO;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.ListFormsDTO;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.ValidationDescription;
import org.jbpm.formbuilder.shared.task.TaskRef;

/**
 * test jaxb dto objects to see if they work properly
 */
public class JaxbDtoTest extends TestCase {

    private GuvnorHelper helper = new GuvnorHelper("", "", "");
    
    public void testPropertiesDTOEmpty() throws Exception {
        PropertiesDTO dto = new PropertiesDTO(new HashMap<String, String>());
        jaxbSimulation(dto, PropertiesDTO.class, PropertiesItemDTO.class);
    }

    public void testPropertiesDTOOneItem() throws Exception {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("oneItemKey", "oneItemValue");
        PropertiesDTO dto = new PropertiesDTO(props);
        jaxbSimulation(dto, PropertiesDTO.class, PropertiesItemDTO.class);
    }
    
    public void testPropertiesDTOManyItems() throws Exception {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("oneItemKey", "oneItemValue");
        props.put("anotherItemKey", "anotherItemValue");
        props.put("andYet", "oneMore");
        PropertiesDTO dto = new PropertiesDTO(props);
        jaxbSimulation(dto, PropertiesDTO.class, PropertiesItemDTO.class);
    }
    
    public void testListValidationsDTOEmpty() throws Exception {
        ListValidationsDTO dto = new ListValidationsDTO();
        jaxbSimulation(dto, ListValidationsDTO.class, ValidationDTO.class, PropertiesItemDTO.class);
    }
    
    public void testListValidationsDTOManyItems() throws Exception {
        List<ValidationDescription> validations = new ArrayList<ValidationDescription>();
        ValidationDescription validation1 = new ValidationDescription();
        validation1.setClassName("org.jbpm.formbuilder.ThisClassDoesntExist");
        validation1.setProperties(new HashMap<String, String>());
        validations.add(validation1);
        ValidationDescription validation2 = new ValidationDescription();
        validation2.setClassName("org.jbpm.formbuilder.client.validation.NotEmptyValidationItem");
        Map<String, String> notEmptyProps = new HashMap<String, String>();
        notEmptyProps.put("message", "aaa");
        notEmptyProps.put("message2", "bbb");
        validation2.setProperties(notEmptyProps);
        validations.add(validation2);
        ValidationDescription validation3 = new ValidationDescription();
        validation3.setClassName("org.jbpm.formbuilder.client.validation.SomethingValidationItem");
        Map<String, String> somethingProps = new HashMap<String, String>();
        somethingProps.put("message", "aaa");
        validation3.setProperties(somethingProps);
        validations.add(validation3);
        ListValidationsDTO dto = new ListValidationsDTO(validations);
        jaxbSimulation(dto, ListValidationsDTO.class, ValidationDTO.class, PropertiesItemDTO.class);
    }
    
    public void testListTasksDTOEmpty() throws Exception {
        ListTasksDTO dto = new ListTasksDTO();
        jaxbSimulation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    public void testListTasksDTOElementsWithNoContent() throws Exception {
        List<TaskRef> tasks = new ArrayList<TaskRef>();
        TaskRef task1 = new TaskRef();
        tasks.add(task1);
        TaskRef task2 = new TaskRef();
        tasks.add(task2);
        ListTasksDTO dto = new ListTasksDTO(tasks);
        jaxbSimulation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    public void testListTasksDTOElementsWithContent() throws Exception {
        List<TaskRef> tasks = new ArrayList<TaskRef>();
        TaskRef task1 = new TaskRef();
        task1.setPackageName("myPackage");
        task1.setProcessId("myProcessId");
        task1.setProcessName("myProcessName");
        task1.setTaskId("myTaskId");
        task1.addInput("myInput1", "myValue1");
        task1.addInput("myInput2", "myValue2");
        task1.addOutput("myOutput1", "myValue3");
        task1.addOutput("myOutput2", "myValue4");
        Map<String, String> metaData1 = new HashMap<String, String>();
        metaData1.put("myMetaDataKey1", "myMetaDataValue1");
        metaData1.put("myMetaDataKey2", "myMetaDataValue2");
        task1.setMetaData(metaData1);
        tasks.add(task1);
        TaskRef task2 = new TaskRef();
        task2.setPackageName("yourPackage");
        task2.setProcessId("yourProcessId");
        task2.setProcessName("yourProcessName");
        task2.setTaskId("yourTaskId");
        task2.addInput("yourInput1", "yourValue1");
        task2.addInput("yourInput2", "yourValue2");
        task2.addOutput("yourOutput1", "yourValue3");
        task2.addOutput("yourOutput2", "yourValue4");
        Map<String, String> metaData2 = new HashMap<String, String>();
        metaData2.put("yourMetaDataKey1", "yourMetaDataValue1");
        metaData2.put("yourMetaDataKey2", "yourMetaDataValue2");
        task2.setMetaData(metaData2);
        tasks.add(task2);
        ListTasksDTO dto = new ListTasksDTO(tasks);
        jaxbSimulation(dto, ListTasksDTO.class, TaskRefDTO.class, PropertyDTO.class, MetaDataDTO.class);
    }
    
    public void testListFormsDTOEmpty() throws Exception {
        ListFormsDTO dto = new ListFormsDTO();
        jaxbSimulation(dto, ListFormsDTO.class, FormDefDTO.class);
        ListFormsDTO dto2 = new ListFormsDTO(new ArrayList<FormRepresentation>());
        jaxbSimulation(dto2, ListFormsDTO.class, FormDefDTO.class);
    }

    public void testListFormsOneItem() throws Exception {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder()); // this is important
        FormRepresentation form = RESTAbstractTest.createMockForm("myForm", "param1", "param2", "param3");
        ListFormsDTO dto = new ListFormsDTO(form);
        jaxbSimulation(dto, ListFormsDTO.class, FormDefDTO.class);
    }
    
    public void testListFormsDTOManyItems() throws Exception {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder()); // this is important
        FormRepresentation form1 = RESTAbstractTest.createMockForm("myForm", "param1", "param2", "param3");
        FormRepresentation form2 = RESTAbstractTest.createMockForm("otherForm", "paramA", "paramB", "paramC");
        List<FormRepresentation> forms = new ArrayList<FormRepresentation>();
        forms.add(form1);
        forms.add(form2);
        ListFormsDTO dto = new ListFormsDTO(forms);
        jaxbSimulation(dto, ListFormsDTO.class, FormDefDTO.class);
    }
    
    public void testListMenuItemsDTOEmpty() throws Exception {
        ListMenuItemsDTO dto = new ListMenuItemsDTO();
        jaxbSimulation(dto, ListMenuItemsDTO.class, MenuGroupDTO.class, MenuItemDTO.class, FormEffectDTO.class);
    }
    
    public void testListMenuItemsDTOEmptyGroups() throws Exception {
        Map<String, List<MenuItemDescription>> items = new HashMap<String, List<MenuItemDescription>>();
        items.put("oneGroup", new ArrayList<MenuItemDescription>());
        items.put("nullGroup", null);
        ListMenuItemsDTO dto = new ListMenuItemsDTO(items);
        jaxbSimulation(dto, ListMenuItemsDTO.class, MenuGroupDTO.class, MenuItemDTO.class, FormEffectDTO.class);
    }
    
    public void testListMenuItemsDTOManyItems() throws Exception {
        Map<String, List<MenuItemDescription>> items = new HashMap<String, List<MenuItemDescription>>();
        List<MenuItemDescription> itemsOfGroup1 = new ArrayList<MenuItemDescription>();
        MenuItemDescription menuItem1 = new MenuItemDescription();
        List<String> allowedEvents = new ArrayList<String>();
        allowedEvents.add("onclick");
        allowedEvents.add("onblur");
        allowedEvents.add("onfocus");
        List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
        FormEffectDescription effect1 = new FormEffectDescription();
        effect1.setClassName("org.jbpm.formbuilder.client.effect.ResizeEffect");
        effects.add(effect1);
        FormEffectDescription effect2 = new FormEffectDescription();
        effect2.setClassName("org.jbpm.formbuilder.client.effect.RemoveEffect");
        effects.add(effect2);
        menuItem1.setAllowedEvents(allowedEvents);
        menuItem1.setClassName("org.jbpm.formbuilder.client.menu.items.ClientScriptMenuItem");
        menuItem1.setEffects(effects);
        MenuItemDescription menuItem2 = new MenuItemDescription();
        menuItem2.setClassName("org.jbpm.formbuilder.client.menu.items.TableLayoutMenuItem");
        menuItem2.setAllowedEvents(allowedEvents);
        itemsOfGroup1.add(menuItem1);
        itemsOfGroup1.add(menuItem2);
        List<MenuItemDescription> itemsOfGroup2 = new ArrayList<MenuItemDescription>();

        MenuItemDescription menuItem3 = new MenuItemDescription();
        menuItem3.setClassName("org.jbpm.formbuilder.client.menu.items.TableLayoutMenuItem");
        menuItem3.setEffects(effects);
        MenuItemDescription menuItem4 = new MenuItemDescription();
        MenuItemDescription menuItem5 = new MenuItemDescription();
        menuItem5.setClassName("org.jbpm.formbuilder.client.menu.items.HeaderMenuItem");
        menuItem5.setEffects(effects);
        menuItem5.setAllowedEvents(allowedEvents);
        menuItem5.setItemRepresentation(RESTAbstractTest.createMockForm("", "param2").getFormItems().iterator().next());
        menuItem5.setName("some name");
        itemsOfGroup2.add(menuItem3);
        itemsOfGroup2.add(menuItem4);
        itemsOfGroup2.add(menuItem5);
        items.put("oneGroup", itemsOfGroup1);
        items.put("twoGroups", itemsOfGroup2);
        ListMenuItemsDTO dto = new ListMenuItemsDTO(items);
        jaxbSimulation(dto, ListMenuItemsDTO.class, MenuGroupDTO.class, MenuItemDTO.class, FormEffectDTO.class);
    }
    
    public void testListOptionsDTOEmpty() throws Exception {
        ListOptionsDTO dto = new ListOptionsDTO();
        jaxbSimulation(dto, ListOptionsDTO.class, MenuOptionDTO.class);
        ListOptionsDTO dto2 = new ListOptionsDTO(new ArrayList<MenuOptionDescription>());
        jaxbSimulation(dto2, ListOptionsDTO.class, MenuOptionDTO.class);
    }
    
    public void testListOptionsManyItems() throws Exception {
        List<MenuOptionDescription> options = new ArrayList<MenuOptionDescription>();
        MenuOptionDescription option1 = new MenuOptionDescription();
        option1.setCommandClass("aaa");
        option1.setHtml("bbb");
        MenuOptionDescription option2 = new MenuOptionDescription();
        List<MenuOptionDescription> subMenu = new ArrayList<MenuOptionDescription>();
        MenuOptionDescription option2_1 = new MenuOptionDescription();
        option2_1.setCommandClass("ccc");
        MenuOptionDescription option2_2 = new MenuOptionDescription();
        subMenu.add(option2_1);
        subMenu.add(option2_2);
        option2.setHtml("eee");
        option2.setSubMenu(subMenu);
        
        options.add(option1);
        options.add(option2);
        ListOptionsDTO dto = new ListOptionsDTO(options);
        jaxbSimulation(dto, ListOptionsDTO.class, MenuOptionDTO.class);
    }
    
    public void testFormPreviewDTO() throws Exception {
        FormPreviewDTO dto = new FormPreviewDTO();
        List<FormPreviewParameterDTO> inputs = new ArrayList<FormPreviewParameterDTO>();
        FormPreviewParameterDTO param1 = new FormPreviewParameterDTO();
        param1.setKey("a");
        param1.setValue("a1");
        FormPreviewParameterDTO param2 = new FormPreviewParameterDTO();
        param2.setKey("b");
        param2.setValue("b1");
        inputs.add(param1);
        inputs.add(param2);
        dto.setInput(inputs);
        dto.setRepresentation("{}");
        jaxbSimulation(dto, FormPreviewDTO.class, FormPreviewParameterDTO.class);
        
        FormPreviewDTO dto2 = new FormPreviewDTO();
        dto.setRepresentation("{}");
        jaxbSimulation(dto2, FormPreviewDTO.class, FormPreviewParameterDTO.class);
        
        FormPreviewDTO dto3 = new FormPreviewDTO();
        jaxbSimulation(dto3, FormPreviewDTO.class, FormPreviewParameterDTO.class);
        
        FormPreviewDTO dto4 = new FormPreviewDTO();
        dto4.setInput(new ArrayList<FormPreviewParameterDTO>());
        jaxbSimulation(dto4, FormPreviewDTO.class, FormPreviewParameterDTO.class);
    }

    private <T> void jaxbSimulation(T dto, Class<T> retType, Class<?>... otherBoundTypes) throws JAXBException, IOException {
        List<Class<?>> boundTypesList = new ArrayList<Class<?>>();
        boundTypesList.add(retType);
        for (Class<?> boundType : otherBoundTypes) {
            if (!boundTypesList.contains(boundType)) {
                boundTypesList.add(boundType);
            }
        }
        Class<?>[] boundTypes = boundTypesList.toArray(new Class<?>[0]);
        String xml = helper.jaxbSerializing(dto, boundTypes);
        assertNotNull("xml shouldn't be null", xml);
        assertTrue("xml should have content", xml.length() > 0);
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
        T dto2 = helper.jaxbTransformation(retType, input, boundTypes);
        assertNotNull("dto2 shouldn't be null", dto2);
        assertEquals("dto2 and dto should be the same", dto, dto2);
    }
}
