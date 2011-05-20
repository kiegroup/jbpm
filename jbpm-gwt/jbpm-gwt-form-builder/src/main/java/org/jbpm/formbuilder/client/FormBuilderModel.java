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

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.menu.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.HorizontalLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.TextFieldMenuItem;

public class FormBuilderModel {

    public List<FBMenuItem> getMenuItems() {
        List<FBMenuItem> list = new ArrayList<FBMenuItem>();
        /*final List<String> classNames = new ArrayList<String>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "api/menuItems");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                String json = response.getText();
                JSONValue value = JSONParser.parseLenient(json);
                if (value.isArray() != null) {
                    JSONArray array = value.isArray();
                    int size = array.size();
                    for (int i = 0; i < size; i++) {
                        classNames.add(array.get(i).isString().stringValue());
                    }
                } else {
                    classNames.add(value.isString().stringValue());
                }
            }
            
            public void onError(Request request, Throwable exception) {
                // TODO Auto-generated method stub
            }
        });
        for (String className : classNames) {
            try {
                Class<?> kclass = ReflectionHelper.loadClass(className);
                Object obj = ReflectionHelper.newInstance(kclass);
                if (obj instanceof FBMenuItem) {
                    list.add((FBMenuItem) obj);
                } else {
                    list.add(new ErrorMenuItem(className + " not of type FBMenuItem"));
                }
            } catch (Exception e) {
                
                //TODO Auto-generated method stub
            }
        }*/
        list.add(new TextFieldMenuItem());
        list.add(new CompleteButtonMenuItem());
        list.add(new LabelMenuItem());
        list.add(new HorizontalLayoutMenuItem());
        return list;
    }
}
