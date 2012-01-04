package org.jbpm.process.instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.common.InternalKnowledgeRuntime;
import org.drools.runtime.process.EventListener;
import org.drools.spi.ProcessContext;
import org.jbpm.process.core.event.EventFilter;
import org.jbpm.process.instance.impl.AssignmentAction;

@SuppressWarnings("serial")
public class StartProcessEventListener implements EventListener, Serializable {
	
    private String              processId;
    private List<EventFilter>   eventFilters;
    private Map<String, String> inMappings;
    private final List<AssignmentAction> actions;
    private transient final InternalProcessRuntime _rt;

    public StartProcessEventListener(String processId,
                                     List<EventFilter> eventFilters,
                                     Map<String, String> inMappings, List<AssignmentAction> actions, ProcessRuntimeImpl rt) {
        this.processId = processId;
        this.eventFilters = eventFilters;
        this.inMappings = inMappings;
        this.actions = actions;
        this._rt = rt;
    }

    public String[] getEventTypes() {
        return null;
    }

    public void signalEvent(String type,
                            Object event) {
        for ( EventFilter filter : eventFilters ) {
            if ( !filter.acceptsEvent( type,
                                       event ) ) {
                return;
            }
        }
        Map<String, Object> params = null;
        if ( inMappings != null && !inMappings.isEmpty() ) {
            params = new HashMap<String, Object>();
            for ( Map.Entry<String, String> entry : inMappings.entrySet() ) {
                if ( "event".equals( entry.getValue() ) ) {
                    params.put( entry.getKey(),
                                event );
                } else {
                    params.put( entry.getKey(),
                                entry.getValue() );
                }
            }
        }
		org.jbpm.process.instance.ProcessInstance startProcess = (org.jbpm.process.instance.ProcessInstance) _rt
				.startProcess(processId, params);

		InternalKnowledgeRuntime knowledgeRuntime = startProcess
				.getKnowledgeRuntime();

		ProcessContext context = new ProcessContext(knowledgeRuntime);

		context.setProcessInstance(startProcess);

		Map<String, Object> metadata = null;
		for (AssignmentAction assignment : actions) {
			try {
				metadata = new HashMap<String, Object>();
				metadata.put(AssignmentAction.START_MESSAGE, event);
				assignment.execute(metadata, context);
			} catch (Exception e) {
				throw new RuntimeException(
						"There is a problem executing a Assignment, the metadata associated is "
								+ metadata, e);

			}
		}	    
	}
}
