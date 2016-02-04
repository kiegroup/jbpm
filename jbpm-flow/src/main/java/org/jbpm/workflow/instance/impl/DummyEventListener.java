package org.jbpm.workflow.instance.impl;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.kie.api.runtime.process.EventListener;

public class DummyEventListener implements EventListener, ProcessImplementationPart {

    public final static DummyEventListener EMPTY_EVENT_LISTENER = new DummyEventListener();

    private DummyEventListener() {
    }

    @Override
    public void signalEvent( String type, Object event ) { }

    @Override
    public String[] getEventTypes() { return null; }

    @Override
    public boolean isStackless() {
        throw new IllegalStateException("Unable to determine whether or not we're stackless!");
    }

}
