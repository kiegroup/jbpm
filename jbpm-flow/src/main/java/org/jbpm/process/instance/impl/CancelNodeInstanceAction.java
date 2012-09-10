package org.jbpm.process.instance.impl;

import java.io.Serializable;

import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessContext;
import org.drools.runtime.process.WorkflowProcessInstance;

public class CancelNodeInstanceAction implements Action, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long attachedToNodeId;
	
	public CancelNodeInstanceAction(Long attachedToNodeId) {
		super();
		this.attachedToNodeId = attachedToNodeId;
	}
	
	public void execute(ProcessContext context) throws Exception {
		WorkflowProcessInstance pi = context.getNodeInstance().getProcessInstance();
		long nodeInstanceId = -1;
		for (NodeInstance nodeInstance : pi.getNodeInstances()) {
			if (attachedToNodeId == nodeInstance.getNodeId()) {
				nodeInstanceId = nodeInstance.getId();
				break;
			}
		}
		((org.jbpm.workflow.instance.NodeInstance)context.getNodeInstance().getProcessInstance().getNodeInstance(nodeInstanceId)).cancel();
	}

}
