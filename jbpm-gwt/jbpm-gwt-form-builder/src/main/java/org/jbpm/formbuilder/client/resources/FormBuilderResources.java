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
package org.jbpm.formbuilder.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface FormBuilderResources extends ClientBundle {

    FormBuilderResources INSTANCE = GWT.create( FormBuilderResources.class );
    
    @Source("images/completeButton.gif")
    ImageResource completeButton();
    
    @Source("images/textField.gif")
    ImageResource textField();
    
    @Source("images/passwordField.gif")
    ImageResource passwordField();
    
    @Source("images/label.gif")
    ImageResource label();

    @Source("images/comboBox.gif")
    ImageResource comboBox();
    
    @Source("images/done_icon.gif")
    ImageResource doneIcon();
    
    @Source("images/remove_icon.gif")
    ImageResource removeIcon();

    @Source("images/error_icon.gif")
    ImageResource errorIcon();

    @Source("images/horizontal_layout_icon.gif")
    ImageResource horizontalLayoutIcon();
    
    @Source("images/table_layout_icon.gif")
    ImageResource tableLayoutIcon();

    @Source("images/border_layout_icon.gif")
	ImageResource borderLayoutIcon();

    @Source("images/header.gif")
    ImageResource header();

    @Source("images/textArea.gif")
    ImageResource textArea();

    @Source("images/hidden.gif")
    ImageResource hidden();

    @Source("images/checkBox.gif")
    ImageResource checkBox();

    @Source("images/fileInput.gif")
    ImageResource fileInput();

    @Source("images/image.gif")
    ImageResource image();

    @Source("images/html.gif")
    ImageResource html();

    @Source("images/default_image_en.jpg")
    ImageResource defaultImage();

    @Source("images/radioButton.gif")
    ImageResource radioButton();

    @Source("images/question.gif")
    ImageResource questionIcon();

    @Source("images/absolute_layout_icon.gif")
    ImageResource absoluteLayoutIcon();

    @Source("images/conditional_block.gif")
    ImageResource conditionalBlock();
    
    @Source("images/loop_block.gif")
    ImageResource loopBlock();
    
    @Source("images/transformation_block.gif")
    ImageResource transformationBlock();
}
