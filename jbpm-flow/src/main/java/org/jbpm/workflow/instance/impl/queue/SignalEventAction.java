package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.workflow.instance.NodeInstance;
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
    public boolean actsOn(ProcessImplementationPart instance) {
        if( eventListener instanceof NodeInstance && instance instanceof NodeInstance ) {
            return ((NodeInstance) eventListener).getUniqueId().equals(((NodeInstance) instance).getUniqueId());
        }
        return false;
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
