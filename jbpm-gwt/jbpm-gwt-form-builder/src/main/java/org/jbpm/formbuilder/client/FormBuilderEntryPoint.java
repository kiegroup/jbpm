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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;

public class FormBuilderEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        EventBus bus = new SimpleEventBus();
        
        Grid mainGrid = new Grid(1, 2);
        Grid editGrid = new Grid(2, 1);
        
        editGrid.add(createMenu(bus));
        editGrid.add(createEdition(bus));
        
        mainGrid.add(editGrid);
        mainGrid.add(createLayout(bus));
        
        RootPanel.get("formBuilder").add(mainGrid);
    }

    private EditionView createEdition(EventBus bus) {
        EditionModel editionModel = new EditionModel();
        EditionView editionView = new EditionView();
        new EditionController(editionModel, editionView, bus);
        return editionView;
    }

    private MenuView createMenu(EventBus bus) {
        MenuModel menuModel = new MenuModel();
        MenuView menuView = new MenuView();
        new MenuController(menuModel, menuView, bus);
        return menuView;
    }

    private LayoutView createLayout(EventBus bus) {
        LayoutModel layoutModel = new LayoutModel();
        LayoutView layoutView = new LayoutView();
        new LayoutController(layoutModel, layoutView, bus);
        return layoutView;
    }
}
