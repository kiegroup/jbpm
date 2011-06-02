package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.edition.EditionPresenter;
import org.jbpm.formbuilder.client.edition.EditionView;
import org.jbpm.formbuilder.client.layout.LayoutPresenter;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.MenuPresenter;
import org.jbpm.formbuilder.client.menu.MenuView;
import org.jbpm.formbuilder.client.options.OptionsPresenter;
import org.jbpm.formbuilder.client.options.OptionsView;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.Grid;

public class FormBuilderController {

    public FormBuilderController(FormBuilderModel model, FormBuilderView view) {
        super();
        PickupDragController dragController = new PickupDragController(view, true);
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        dragController.registerDropController(new DisposeDropController(view));
        
        Grid mainGrid = new Grid(2, 1);
        
        Grid toolGrid = new Grid(1, 2);
        Grid editGrid = new Grid(2, 1);
        
        editGrid.setWidget(0, 0, createMenu(model));
        editGrid.setWidget(1, 0, createEdition());
        
        toolGrid.setWidget(0, 0, editGrid);
        toolGrid.setWidget(0, 1, createLayout());
        
        mainGrid.setWidget(0, 0, createOptions(model));
        mainGrid.setWidget(1, 0, toolGrid);
        
        view.add(mainGrid);
    }

    private EditionView createEdition() {
        EditionView view = new EditionView();
        new EditionPresenter(view);
        return view;
    }

    private MenuView createMenu(FormBuilderModel model) {
        MenuView view = new MenuView();
        new MenuPresenter(model.getMenuItems(), view);
        return view;
    }

    private LayoutView createLayout() {
        LayoutView view = new LayoutView();
        new LayoutPresenter(view);
        return view;
    }
    
    private OptionsView createOptions(FormBuilderModel model) {
        OptionsView view = new OptionsView();
        new OptionsPresenter(model, view);
        return view;
    }
}
