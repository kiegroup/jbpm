package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.GwtEvent;

public class RepresentationFactoryPopulatedEvent extends GwtEvent<RepresentationFactoryPopulatedHandler> {

    public static final Type<RepresentationFactoryPopulatedHandler> TYPE = new Type<RepresentationFactoryPopulatedHandler>();
    
    @Override
    public Type<RepresentationFactoryPopulatedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RepresentationFactoryPopulatedHandler handler) {
        handler.onEvent(this);
    }

}
