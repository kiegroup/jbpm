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
    
    @Source("images/label.gif")
    ImageResource label();

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
}
