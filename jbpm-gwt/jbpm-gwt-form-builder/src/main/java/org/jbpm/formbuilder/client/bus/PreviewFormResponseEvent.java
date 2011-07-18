package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class PreviewFormResponseEvent extends GwtEvent<PreviewFormResponseHandler> {

    public static final Type<PreviewFormResponseHandler> TYPE = new Type<PreviewFormResponseHandler>();
    
    private final String html;
    
    public PreviewFormResponseEvent(String html) {
        super();
        this.html = html;
    }

    public String getHtml() {
        return html;
    }
    
    @Override
    public Type<PreviewFormResponseHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PreviewFormResponseHandler handler) {
        handler.onServerResponse(this);
    }

}
