package org.jbpm.persistence.mapdb;

import java.util.Map;

import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.process.ProcessInstance;

public class MapDBStoringService {

	public static void storeVariables(MapDBProcessInstance instance, Environment environment, ProcessIndexService procIdx) {
		if (environment == null) {
			return;
		}
		ObjectStoringStrategy[] strategies = (ObjectStoringStrategy[])
				environment.get(MapDBEnvironmentName.OBJECT_STORING_STRATEGIES);
		if (strategies == null) {
			return;
		}
		if (instance == null) {
			return;
		}
		ProcessInstance pInstance = instance.getProcessInstance();
		if (pInstance == null) {
			return;
		}
		WorkflowProcessInstanceImpl wfInstance = (WorkflowProcessInstanceImpl) pInstance;
		Map<String, Object> variables = wfInstance.getVariables();
		if (variables == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			String varName = entry.getKey();
			procIdx.addByVarName(varName, instance.getId());
			Object var = entry.getValue();
			for (ObjectStoringStrategy strat : strategies) {
				if (strat.accept(var)) {
					strat.persist(var);
					break;
				}
			}
		}
	}

}
