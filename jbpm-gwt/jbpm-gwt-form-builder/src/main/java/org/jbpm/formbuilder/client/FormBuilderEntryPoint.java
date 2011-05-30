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

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;

public class FormBuilderEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        FormBuilderModel model = new FormBuilderModel();
        
        AbsolutePanel panel = new AbsolutePanel();
        PickupDragController dragController = new PickupDragController(panel, true);
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        dragController.registerDropController(new DisposeDropController(panel));
        
        Grid mainGrid = new Grid(2, 1);
        
        Grid toolGrid = new Grid(1, 2);
        Grid editGrid = new Grid(2, 1);
        
        editGrid.setWidget(0, 0, createMenu(model));
        editGrid.setWidget(1, 0, createEdition(model));
        
        toolGrid.setWidget(0, 0, editGrid);
        toolGrid.setWidget(0, 1, createLayout(model));
        
        mainGrid.setWidget(0, 0, createOptions(model));
        mainGrid.setWidget(1, 0, toolGrid);
        
        panel.add(mainGrid);
        
        RootPanel.get("formBuilder").add(panel);
    }

    private EditionView createEdition(FormBuilderModel model) {
        EditionView view = new EditionView();
        new EditionController(model, view);
        return view;
    }

    private MenuView createMenu(FormBuilderModel model) {
        MenuView view = new MenuView();
        new MenuController(model, view);
        return view;
    }

    private LayoutView createLayout(FormBuilderModel model) {
        LayoutView view = new LayoutView();
        new LayoutController(model, view);
        return view;
    }
    
    private OptionsView createOptions(FormBuilderModel model) {
        OptionsView view = new OptionsView();
        new OptionsPresenter(model, view);
        return view;
    }
}
