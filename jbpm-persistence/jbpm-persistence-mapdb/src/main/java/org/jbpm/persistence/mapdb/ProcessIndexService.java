package org.jbpm.persistence.mapdb;

import java.util.Arrays;

import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class ProcessIndexService {

	private static BTreeMap<ProcessKey, PersistentProcessInstance> byKey;
	private static HTreeMap<String, long[]> byVarName;
	
	private ObjectStoringStrategy[] strategies;

	private static synchronized void init(DB db) {
		if (byKey == null || byKey.isClosed()) {
			byKey = db.treeMap(new MapDBProcessInstance().getMapKey(), 
					new ProcessInstanceKeySerializer(), 
					new PersistentProcessInstanceSerializer()).createOrOpen();
			byVarName = db.hashMap("processByVarName", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
		}
	}

	public ProcessIndexService(DB db, ObjectStoringStrategy[] strategies) {
		init(db);
		this.strategies = strategies;
	}

	public void update(MapDBProcessInstance instance) {
		/*BTreeMap<Long, PersistentProcessInstance> byId = db.treeMap(
		getMapKey() + "ById", Serializer.LONG,
		new PersistentProcessInstanceSerializer()).open();
		byId.put(getId(), this);
		BTreeMap<String, long[]> byEventTypes = db.treeMap(
				getMapKey() + "ByEventTypes", 
				Serializer.STRING, Serializer.LONG_ARRAY).open();
		if (getEventTypes() != null) {
			for (String eventType : getEventTypes()) {
				long[] ids = new long[] { getId() };
				if (byEventTypes.containsKey(eventType)) {
					long[] otherIds = byEventTypes.get(eventType);
					ids = Arrays.copyOf(otherIds, otherIds.length + 1);
					ids[ids.length - 1] = getId();
				}
				byEventTypes.put(eventType, ids);
			}
		}
		 */
		ProcessKey key = new ProcessKey(instance.getId(), instance.getEventTypes(), null);
		byKey.put(key, instance);
		ProcessInstance pInstance = instance.getProcessInstance();
		if (pInstance != null) {
			WorkflowProcessInstanceImpl wfInstance = (WorkflowProcessInstanceImpl) pInstance;
			MapDBStoringService.storeVariables(instance, instance.getEnvironment(), this);
			for (NodeInstance nodeInst : wfInstance.getNodeInstances()) {
				if (nodeInst instanceof CompositeContextNodeInstance) {
					CompositeContextNodeInstance ccni = (CompositeContextNodeInstance) nodeInst;
					VariableScopeInstance vsi = (VariableScopeInstance) ccni.getContextInstance(VariableScope.VARIABLE_SCOPE);
					if (vsi != null && vsi.getVariables() != null) {
						for (Object value : vsi.getVariables().values()) {
							strategyStore(value);
						}
					}
				}
			}
		}
		
		
	}
	
	private void strategyStore(Object object) {
		if (strategies != null) {
			for (ObjectStoringStrategy strategy : strategies) {
				if (strategy.accept(object)) {
					strategy.persist(object);
					break;
				}
			}
		}
	}
	
	public BTreeMap<ProcessKey, PersistentProcessInstance> getByKey() {
		return byKey;
	}
	
	public HTreeMap<String, long[]> getByVarName() {
		return byVarName;
	}

	public void addByVarName(String varName, Long processInstanceId) {
		long[] values = byVarName.get(varName);
		if (values == null) {
			values = new long[1];
		} else {
			values = Arrays.copyOf(values, values.length + 1);
		}
		values[values.length - 1] = processInstanceId;
		byVarName.put(varName, values);
	}
}
