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

/**
 * Images should be loaded from here
 */
public interface FormBuilderResources extends ClientBundle {

    FormBuilderResources INSTANCE = GWT.create( FormBuilderResources.class );
    
    @Source("images/completeButton.png")
    ImageResource completeButton();
    
    @Source("images/textField.png")
    ImageResource textField();
    
    @Source("images/passwordField.png")
    ImageResource passwordField();
    
    @Source("images/label.gif")
    ImageResource label();

    @Source("images/comboBox.png")
    ImageResource comboBox();
    
    @Source("images/error_icon.png")
    ImageResource errorIcon();

    @Source("images/horizontal_layout_icon.gif")
    ImageResource horizontalLayoutIcon();
    
    @Source("images/table_layout_icon.png")
    ImageResource tableLayoutIcon();

    @Source("images/border_layout_icon.png")
	ImageResource borderLayoutIcon();

    @Source("images/header.png")
    ImageResource header();

    @Source("images/textArea.gif")
    ImageResource textArea();

    @Source("images/hidden.gif")
    ImageResource hidden();

    @Source("images/checkBox.png")
    ImageResource checkBox();

    @Source("images/fileInput.png")
    ImageResource fileInput();

    @Source("images/image.png")
    ImageResource image();

    @Source("images/html.gif")
    ImageResource html();

    @Source("images/default_image_en.jpg")
    ImageResource defaultImage();

    @Source("images/radioButton.gif")
    ImageResource radioButton();

    @Source("images/question.gif")
    ImageResource questionIcon();

    @Source("images/absolute_layout_icon.png")
    ImageResource absoluteLayoutIcon();

    @Source("images/css_layout_icon.png")
    ImageResource cssLayoutIcon();
    
    @Source("images/conditional_block.png")
    ImageResource conditionalBlock();
    
    @Source("images/loop_block.png")
    ImageResource loopBlock();
    
    @Source("images/transformation_block.gif")
    ImageResource transformationBlock();

    @Source("images/save_button.gif")
    ImageResource saveButton();

    @Source("images/refresh_button.gif")
    ImageResource refreshButton();

    @Source("images/flow_layout_icon.png")
	ImageResource flowLayoutIcon();

    @Source("images/lineGraph.gif")
    ImageResource lineGraph();

    @Source("images/treeFolder.png")
    ImageResource treeFolder();

    @Source("images/treeLeaf.png")
    ImageResource treeLeaf();
}
