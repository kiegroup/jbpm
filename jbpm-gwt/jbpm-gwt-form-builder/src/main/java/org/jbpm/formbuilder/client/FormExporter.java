package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.EventBus;

public class FormExporter {

    private static final String EXPORT_TYPE = FormExporter.class.getName();
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public FormExporter() {
        bus.addHandler(FormItemRemovedEvent.TYPE, new FormItemRemovedHandler() {
            public void onEvent(FormItemRemovedEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(EXPORT_TYPE));
            }
        });
        bus.addHandler(FormItemAddedEvent.TYPE, new FormItemAddedHandler() {
            public void onEvent(FormItemAddedEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(EXPORT_TYPE));
            }
        }); 
        
        bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            public void onEvent(GetFormRepresentationResponseEvent event) {
                if (EXPORT_TYPE.equals(event.getSaveType())) {
                    exportForm(event.getRepresentation());
                }
            }
        });
    }
    
    protected void exportForm(FormRepresentation form) {
        FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
        try {
            String formAsJson = encoder.encode(form);
            setClientExportForm(formAsJson);
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't export form representation as json", e));
        }
    }

    protected final native void start() /*-{
        if (typeof($wnd.clientExportForm) == 'undefined') {
            $wnd.clientExportForm = "";
        } else if ($wnd.clientExportForm == null) {
            $wnd.clientExportForm = "";
        }
    }-*/;
    
    private final native void setClientExportForm(String formAsJson) /*-{
        $wnd.clientExportForm = formAsJson;
    }-*/;
}
