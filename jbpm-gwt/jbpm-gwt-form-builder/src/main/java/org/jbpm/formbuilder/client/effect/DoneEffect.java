package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;

public class DoneEffect extends FBFormEffect {

    public DoneEffect() {
        super("Done", false);
    }
    
    @Override
    protected void createStyles() {
        getItem().fireSelectionEvent(new FormItemSelectionEvent(getItem(), false));
    }
}
