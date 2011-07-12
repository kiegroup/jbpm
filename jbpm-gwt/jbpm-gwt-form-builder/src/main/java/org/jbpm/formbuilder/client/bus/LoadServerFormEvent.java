package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class LoadServerFormEvent extends GwtEvent<LoadServerFormHandler> {

    public static final Type<LoadServerFormHandler> TYPE = new Type<LoadServerFormHandler>();
    
    private final String formName;
    
    public LoadServerFormEvent() {
        this(null);
    }
    
    public LoadServerFormEvent(String formName) {
        super();
        this.formName = formName;
    }
    
    public String getFormName() {
        return formName;
    }

    @Override
    public Type<LoadServerFormHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LoadServerFormHandler handler) {
        handler.onEvent(this);
    }

}
