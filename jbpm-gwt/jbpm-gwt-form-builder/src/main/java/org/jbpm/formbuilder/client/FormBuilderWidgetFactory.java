/**
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
package org.jbpm.formbuilder.client;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.controls.TextFieldWidget;
import org.jbpm.formbuilder.client.eventlisteners.CompleteButtonWidget;
import org.jbpm.formbuilder.client.eventlisteners.LabelWidget;
import org.jbpm.formbuilder.client.menu.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.TextFieldMenuItem;

public class FormBuilderWidgetFactory {

    private final Map<String, FormBuilderWidget> map = new HashMap<String, FormBuilderWidget>();
    public static final FormBuilderWidgetFactory INSTANCE = new FormBuilderWidgetFactory();
    
    private FormBuilderWidgetFactory() {
        //TODO make this get populated from a configuration file (maybe an MVEL file?)
        map.put(LabelMenuItem.class.getName(), new LabelWidget());
        map.put(TextFieldMenuItem.class.getName(), new TextFieldWidget());
        map.put(CompleteButtonMenuItem.class.getName(), new CompleteButtonWidget());
    }
    
    public static FormBuilderWidget getInstance(String itemId) throws WidgetFactoryException {
        return INSTANCE.map.get(itemId);
    }
}
