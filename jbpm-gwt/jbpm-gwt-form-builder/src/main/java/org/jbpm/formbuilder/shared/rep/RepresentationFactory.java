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
package org.jbpm.formbuilder.shared.rep;

import java.util.HashMap; 
import java.util.Map;

public class RepresentationFactory {

    private static final Map<String, String> MAPPING = new HashMap<String, String>();
    
    //TODO make this cleaner and configurable
    static {
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.AbsolutePanelRepresentation", 
                "org.jbpm.formbuilder.client.form.items.AbsoluteLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.BorderPanelRepresentation",
        		"org.jbpm.formbuilder.client.form.items.BorderLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.CheckBoxRepresentation",
                "org.jbpm.formbuilder.client.form.items.CheckBoxFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation", 
                "org.jbpm.formbuilder.client.form.items.ComboBoxFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation",
                "org.jbpm.formbuilder.client.form.items.CompleteButtonFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.ConditionalBlockRepresentation",
                "org.jbpm.formbuilder.client.form.items.ConditionalBlockFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.CSSPanelRepresentation",
        "org.jbpm.formbuilder.client.form.items.CSSLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.FileInputRepresentation",
                "org.jbpm.formbuilder.client.form.items.FileInputFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.FlowPanelRepresentation", 
        		"org.jbpm.formbuilder.client.form.items.FlowLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation",
                "org.jbpm.formbuilder.client.form.items.HeaderFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.HiddenRepresentation",
                "org.jbpm.formbuilder.client.form.items.HiddenFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.HorizontalPanelRepresentation",
                "org.jbpm.formbuilder.client.form.items.HorizontalLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.HTMLRepresentation",
                "org.jbpm.formbuilder.client.form.items.HTMLFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.ImageRepresentation",
                "org.jbpm.formbuilder.client.form.items.ImageFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.LabelRepresentation",
                "org.jbpm.formbuilder.client.form.items.LabelFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.LineGraphRepresentation",
        "org.jbpm.formbuilder.client.form.items.LineGraphFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.LoopBlockRepresentation",
                "org.jbpm.formbuilder.client.form.items.LoopBlockFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.PasswordFieldRepresentation",
                "org.jbpm.formbuilder.client.form.items.PasswordFieldFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.RadioButtonRepresentation",
                "org.jbpm.formbuilder.client.form.items.RadioButtonFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.ServerTransformationRepresentation",
                "org.jbpm.formbuilder.client.form.items.ServerTransformationFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.TableRepresentation",
                "org.jbpm.formbuilder.client.form.items.TableLayoutFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.TextAreaRepresentation",
                "org.jbpm.formbuilder.client.form.items.TextAreaFormItem");
        registerItemClassName("org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation",
                "org.jbpm.formbuilder.client.form.items.TextFieldFormItem");
        
        registerItemClassName("org.jbpm.formbuilder.shared.rep.validation.NotEmptyValidation", 
                "org.jbpm.formbuilder.client.validation.NotEmptyValidationItem");
    }

    public static void registerItemClassName(String repClassName, String itemClassName) {
        MAPPING.put(repClassName, itemClassName);
    }
    
    public static String getItemClassName(String repClassName) {
        return MAPPING.get(repClassName);
    }
    
}
