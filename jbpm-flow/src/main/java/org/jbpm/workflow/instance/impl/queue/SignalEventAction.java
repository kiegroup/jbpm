package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.impl.DummyEventListener;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.runtime.process.EventListener;

public class SignalEventAction implements ProcessActionTrigger {

    private final EventListener eventListener;
    private final String type;
    private final Object event;

    public SignalEventAction( EventListener eventListener, String type, Object event ) {
        this.eventListener = eventListener;
        this.type = type;
        this.event = event;
    }

    @Override
    public void trigger() {
        eventListener.signalEvent(type, event);
    }

    @Override
    public String getUniqueInstanceId() {
        return String.valueOf(((NodeInstanceImpl) eventListener).getUniqueId());
    }

    @Override
    public String toString() {
        String uniqueId;
        if( eventListener instanceof NodeInstanceImpl ) {
            uniqueId = (String) ((NodeInstanceImpl) eventListener).getMetaData().get("UniqueId");
        } else {
            uniqueId = "dummy";
        }
        String eventType = event == null ? "null" : event.getClass().getSimpleName();
        return eventListener.getClass().getSimpleName() + "[" + uniqueId + "].signalEvent(" + type + ", " + eventType + ")";
    }

}
