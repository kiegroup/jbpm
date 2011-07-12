package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

public class PasteFormEffect extends FBFormEffect {

    public PasteFormEffect() {
        super("Paste", false);
    }
    
    @Override
    protected void createStyles() {
        FormBuilderGlobals.getInstance().paste().append(getItem()).execute();
    }

}
