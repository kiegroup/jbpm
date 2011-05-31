package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class SaveFormRepresentationEvent extends
        GwtEvent<SaveFormRepresentationEventHandler> {

    public static final Type<SaveFormRepresentationEventHandler> TYPE = new Type<SaveFormRepresentationEventHandler>();
    
    private final FormRepresentation representation;
    private final String saveType;
    
    public SaveFormRepresentationEvent(FormRepresentation representation, String saveType) {
        super();
        this.representation = representation;
        this.saveType = saveType;
    }

    public FormRepresentation getRepresentation() {
        return representation;
    }
    
    public String getSaveType() {
        return saveType;
    }

    @Override
    public Type<SaveFormRepresentationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SaveFormRepresentationEventHandler handler) {
        handler.onEvent(this);
    }

}
