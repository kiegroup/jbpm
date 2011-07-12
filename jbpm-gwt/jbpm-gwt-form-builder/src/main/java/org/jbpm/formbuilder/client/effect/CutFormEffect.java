package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

public class CutFormEffect extends FBFormEffect {

    public CutFormEffect() {
        super("Cut", false);
    }
    
    @Override
    protected void createStyles() {
        FormBuilderGlobals.getInstance().cut().append(getItem()).execute();
    }

}
