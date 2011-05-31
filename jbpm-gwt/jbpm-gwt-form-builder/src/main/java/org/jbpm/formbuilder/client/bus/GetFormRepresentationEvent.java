package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class GetFormRepresentationEvent extends
        GwtEvent<GetFormRepresentationEventHandler> {

    public static final Type<GetFormRepresentationEventHandler> TYPE = new Type<GetFormRepresentationEventHandler>();
    
    private final String saveType;
    
    public GetFormRepresentationEvent(String saveType) {
        this.saveType = saveType;
    }
    
    public String getSaveType() {
        return saveType;
    }

    @Override
    public Type<GetFormRepresentationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GetFormRepresentationEventHandler handler) {
        handler.onEvent(this);
    }

}
