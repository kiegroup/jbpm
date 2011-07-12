package org.jbpm.formbuilder.client.bus;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class LoadServerFormResponseEvent extends GwtEvent<LoadServerFormResponseHandler> {

    public static final Type<LoadServerFormResponseHandler> TYPE = new Type<LoadServerFormResponseHandler>();
    
    private final List<FormRepresentation> list;
    private final FormRepresentation form;
    
    public LoadServerFormResponseEvent(FormRepresentation formRepresentation) {
        this.list = null;
        this.form = formRepresentation;
    }
    
    public LoadServerFormResponseEvent(List<FormRepresentation> loadedForms) {
        this.list = loadedForms;
        this.form = null;
    }

    public FormRepresentation getForm() {
        return form;
    }
    
    public List<FormRepresentation> getList() {
        return list;
    }
    
    @Override
    public Type<LoadServerFormResponseHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LoadServerFormResponseHandler handler) {
        if (this.form != null) {
            handler.onGetForm(this);
        } else if (this.list != null) {
            handler.onListForms(this);
        }
    }

}
