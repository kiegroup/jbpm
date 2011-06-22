package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class PreviewFormCommand implements BaseCommand {

    protected final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final String saveType;
    
    public PreviewFormCommand(final String saveType) {
        this.saveType = saveType;
        this.bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                FormRepresentation form = event.getRepresentation();
                if (saveType.equals(event.getSaveType())) {
                    saveForm(form);
                }
            }
        });
    }
    
    public void setItem(MenuItem item) {
        /* not implemented */
    }
    
    public void execute() {
        this.bus.fireEvent(new GetFormRepresentationEvent(this.saveType));
    }

    protected void refreshPopupForURL(String url) {
        // TODO This needs to have a control to edit input values, and even to check for expected outputs 
        PopupPanel panel = new PopupPanel(true);
        Frame frame = new Frame(url);
        int height = RootPanel.getBodyElement().getClientHeight();
        int width = RootPanel.getBodyElement().getClientWidth();
        int left = RootPanel.getBodyElement().getAbsoluteLeft();
        int top = RootPanel.getBodyElement().getAbsoluteTop();
        panel.setPixelSize(width - 200, height - 200);
        frame.setPixelSize(width - 200, height - 200);
        panel.setPopupPosition(left + 100, top + 100);
        panel.setWidget(frame);
        panel.show();
    }

    public abstract void saveForm(FormRepresentation form);

}
