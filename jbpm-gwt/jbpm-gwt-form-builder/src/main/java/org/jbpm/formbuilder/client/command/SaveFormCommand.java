package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.menu.FormDataPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class SaveFormCommand implements BaseCommand {

    private static final String SAVE_TYPE = SaveFormCommand.class.getName();
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService service = FormBuilderGlobals.getInstance().getService();
    
    public SaveFormCommand() {
        super();
        bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                if (SAVE_TYPE.equals(event.getSaveType())) {
                    FormRepresentation form = event.getRepresentation();
                    saveForm(form);
                }
            }
        });
    }
    
    private void saveForm(final FormRepresentation form) {
        if (form.getName() == null || "".equals(form.getName())) {
            final FormDataPopupPanel panel = new FormDataPopupPanel(true);
            panel.setAction(form.getAction());
            panel.setEnctype(form.getEnctype());
            panel.setMethod(form.getMethod());
            panel.setName(form.getName());
            panel.setTaskId(form.getTaskId());
            panel.setModal(true);
            panel.setPopupPosition(
                    RootPanel.getBodyElement().getClientWidth() / 2 - 150, 
                    RootPanel.getBodyElement().getClientHeight() / 2 - 150);
            panel.show();
            panel.addCloseHandler(new CloseHandler<PopupPanel>() {
                public void onClose(CloseEvent<PopupPanel> event) {
                    String formName = panel.getFormName();
                    if (formName != null && !"".equals(formName)) {
                        try {
                            form.populate(panel.getFormName(), panel.getTaskId(), panel.getAction(), 
                                    panel.getMethod(), panel.getEnctype(), panel.getDocumentation());
                            service.saveForm(form);
                            bus.fireEvent(new NotificationEvent(Level.INFO, "Form " + form.getName() + " saved successfully"));
                        } catch (FormBuilderException e) {
                            bus.fireEvent(new NotificationEvent(Level.ERROR, "Problem saving form " + form.getName(), e));
                        }
                    } else {
                        bus.fireEvent(new NotificationEvent(Level.WARN, "Cannot save form until a form name is defined"));
                    }
                }
            });
        }
    }
    
    public void execute() {
        bus.fireEvent(new GetFormRepresentationEvent(SAVE_TYPE));
    }

    public void setItem(MenuItem item) {
        item.setEnabled(true);
    }
}
