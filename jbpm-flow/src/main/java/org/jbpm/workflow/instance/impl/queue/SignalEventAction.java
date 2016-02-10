package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.kie.api.runtime.process.EventSignallable;

public class SignalEventAction implements ProcessInstanceAction {

    private final EventSignallable eventListener;
    private final String type;
    private final Object event;

    public SignalEventAction( EventSignallable eventSignallable, String type, Object event ) {
        this.eventListener = eventSignallable;
        this.type = type;
        this.event = event;
    }

    @Override
    public void trigger() {
        eventListener.signalEvent(type, event);
    }

    @Override
    public String getUniqueInstanceId() {
        // OCRAM getUniqueInstanceId => equals(NodeInstance)
        if( eventListener instanceof NodeInstanceImpl ) {
            return String.valueOf(((NodeInstanceImpl) eventListener).getUniqueId());
        } else {
            // OCRAM? is this even needed?
            return null;
        }
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
