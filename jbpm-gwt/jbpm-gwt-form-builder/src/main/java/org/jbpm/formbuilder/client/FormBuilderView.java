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

import org.jbpm.formbuilder.client.edition.EditionView;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.MenuView;
import org.jbpm.formbuilder.client.notification.NotificationsView;
import org.jbpm.formbuilder.client.options.OptionsView;
import org.jbpm.formbuilder.client.tasks.TasksView;
import org.jbpm.formbuilder.client.toolbar.ToolBarView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Main view. Uses UIBinder to define the correct position of components
 */
public class FormBuilderView extends AbsolutePanel {

    private static FBUiBinder uiBinder = GWT.create(FBUiBinder.class);

    interface FBUiBinder extends UiBinder<Widget, FormBuilderView> {
    }
    
    @UiField(provided=true) ScrollPanel treeView;
    @UiField(provided=true) SimplePanel optionsView;
    @UiField(provided=true) ScrollPanel menuView;
    @UiField(provided=true) ScrollPanel editionView;
    @UiField(provided=true) ScrollPanel layoutView;
    @UiField(provided=true) AbsolutePanel toolBarView;
    @UiField(provided=true) AbsolutePanel tasksView;
    @UiField(provided=true) FocusPanel notificationsView;

    protected final void checkBinding() {
        if (timeToBind()) {
            Widget widget = uiBinder.createAndBindUi(this);
            setSize("100%", "100%");
            widget.setSize("100%", "100%");
            add(widget);
        }
    }

    protected boolean timeToBind() {
        return getWidgetCount() == 0 &&
            treeView != null && optionsView != null &&
            menuView != null && editionView != null &&
            layoutView != null && toolBarView != null &&
            tasksView != null && notificationsView != null;
    }

    public void setMenuView(MenuView menuView) {
        this.menuView = menuView;
        checkBinding();
    }
    
    public void setEditionView(EditionView editionView) {
        this.editionView = editionView;
        checkBinding();
    }

    public void setLayoutView(LayoutView layoutView) {
        this.layoutView = layoutView;
        checkBinding();
    }
    
    public void setOptionsView(OptionsView optionsView) {
        this.optionsView = optionsView;
        checkBinding();
    }
    
    public void setTasksView(TasksView tasksView) {
        this.tasksView = tasksView;
        checkBinding();
    }
    
    public void setToolBarView(ToolBarView toolBarView) {
        this.toolBarView = toolBarView;
        checkBinding();
    }
    
    public void setNotificationsView(NotificationsView notificationsView) {
        this.notificationsView = notificationsView;
        checkBinding();
    }
    
    public void setTreeView(ScrollPanel treeView) {
        this.treeView = treeView;
        checkBinding();
    }
}
