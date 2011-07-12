package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

public class CopyFormEffect extends FBFormEffect {

    public CopyFormEffect() {
        super("Copy", false);
    }

    @Override
    protected void createStyles() {
        FormBuilderGlobals.getInstance().copy().append(getItem()).execute();
    }

}
