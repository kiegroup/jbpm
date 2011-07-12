package org.jbpm.formbuilder.client.toolbar;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.LoadServerFormEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.command.LoadFormCommand;
import org.jbpm.formbuilder.client.command.SaveFormCommand;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;

public class ToolBarPresenter {

    private static final String SAVE_TYPE = SaveFormCommand.class.getName();
    private static final String LOAD_TYPE = LoadFormCommand.class.getName();
    
    private final ToolBarView view;
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService service = FormBuilderGlobals.getInstance().getService();

    public ToolBarPresenter(ToolBarView toolBarView) {
        this.view = toolBarView;
        this.bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                if (SAVE_TYPE.equals(event.getSaveType())) {
                    FormRepresentation form = event.getRepresentation();
                    try {
                        service.saveForm(form);
                        bus.fireEvent(new NotificationEvent(Level.INFO, "form" + form.getName() + " saved successfully"));
                    } catch (FormBuilderException e) {
                        bus.fireEvent(new NotificationEvent(Level.ERROR, "Problem saving form", e));
                    }
                }
            }
        });
        this.view.addButton(FormBuilderResources.INSTANCE.saveButton(), "Save", new ClickHandler() {
            public void onClick(ClickEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(SAVE_TYPE));
            }
        });
        
        this.view.addButton(FormBuilderResources.INSTANCE.refreshButton(), "Refresh from Server", new ClickHandler() {
            public void onClick(ClickEvent event) {
                final ToolbarDialog dialog = view.createToolbarDialog(
                        "Attention! if you continue, all data you haven't saved will be lost and " +
                        "replaced with the server information. Are you sure you want to continue?");
                dialog.addOkButtonHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        bus.fireEvent(new GetFormRepresentationEvent(LOAD_TYPE));
                    }
                });
                dialog.show();
            }
        });
        bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                if (LOAD_TYPE.equals(event.getSaveType())) {
                    bus.fireEvent(new LoadServerFormEvent(event.getRepresentation().getName()));
                }
            }
        });
    }
}
