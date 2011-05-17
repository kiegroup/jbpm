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

public class FormBuilderWidgetFactory {

    public static FormBuilderWidget getInstance(String itemId) throws WidgetFactoryException {
        String widgetClassName = itemId.replace("menu", "widget").replace("MenuItem", "Widget");
        try {
            Class<?> widgetClass = Class.forName(widgetClassName);
            Object widgetObj = widgetClass.newInstance();
            return (FormBuilderWidget) widgetObj;
        } catch (ClassNotFoundException e) {
            throw new WidgetFactoryException("widget class " + widgetClassName 
                    + " not found for menu item " + itemId, e);
        } catch (IllegalAccessException e) {
            throw new WidgetFactoryException("widget class " + widgetClassName 
                    + " inaccessible constructor", e);
        } catch (InstantiationException e) {
            throw new WidgetFactoryException("widget class " + widgetClassName 
                    + " constructor threw error", e);
        }
    }
}
