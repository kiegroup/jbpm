package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class PreviewFormRepresentationEvent extends
        GwtEvent<PreviewFormRepresentationEventHandler> {

    public static final Type<PreviewFormRepresentationEventHandler> TYPE = new Type<PreviewFormRepresentationEventHandler>();
    
    private final FormRepresentation representation;
    private final String saveType;
    
    public PreviewFormRepresentationEvent(FormRepresentation representation, String saveType) {
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
    public Type<PreviewFormRepresentationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PreviewFormRepresentationEventHandler handler) {
        handler.onEvent(this);
    }

}
