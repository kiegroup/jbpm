package org.jbpm.persistence.mapdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionManagerHelper;
import org.drools.persistence.mapdb.MapDBPersistenceContext;
import org.jbpm.persistence.PersistentCorrelationKey;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.internal.process.CorrelationKey;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

public class MapDBProcessPersistenceContext  extends MapDBPersistenceContext
	implements ProcessPersistenceContext{

	private final Atomic.Long nextId;
	//private final BTreeMap<String, long[]> mapByEventTypes;
	//private final BTreeMap<Long, PersistentProcessInstance> mapById;
	//private final BTreeMap<PersistentCorrelationKey, Long> mapByCK;
	private final BTreeMap<ProcessKey, PersistentProcessInstance> map;
	
	public MapDBProcessPersistenceContext(DB db, TransactionManager txm, ObjectStoringStrategy[] strategies) {
		super(db, txm, strategies);
		String keyPrefix = new MapDBProcessInstance().getMapKey();
		/*this.mapById = db.treeMap(keyPrefix + "ById", 
				Serializer.LONG, new PersistentProcessInstanceSerializer()).createOrOpen();
		this.mapByEventTypes = db.treeMap(keyPrefix + "ByEventTypes",
				Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
		this.mapByCK = db.treeMap(keyPrefix + "ByCK", 
				new PersistentCorrelationKeySerializer(), Serializer.LONG).createOrOpen();*/
		this.map = db.treeMap(keyPrefix, new ProcessInstanceKeySerializer(), new PersistentProcessInstanceSerializer()).createOrOpen();
		nextId = db.atomicLong("processId").createOrOpen();
	}
	
	@Override
	public PersistentProcessInstance persist(PersistentProcessInstance processInstanceInfo) {
		long id = -1;
		processInstanceInfo.transform();
		if (processInstanceInfo.getId() == null || processInstanceInfo.getId() == -1) {
			id = nextId.incrementAndGet();
			processInstanceInfo.setId(id);
		} else {
			id = processInstanceInfo.getId();
		}
		/*mapById.put(processInstanceInfo.getId(), processInstanceInfo);
		if (processInstanceInfo.getEventTypes() != null) {
			for (String eventType : processInstanceInfo.getEventTypes()) {
				long[] ids = new long[] { processInstanceInfo.getId() };
				if (mapByEventTypes.containsKey(eventType)) {
					long[] otherIds = mapByEventTypes.get(eventType);
					ids = Arrays.copyOf(otherIds, otherIds.length + 1);
					ids[ids.length - 1] = processInstanceInfo.getId();
				}
				mapByEventTypes.put(eventType, ids);
			}
		}*/
		ProcessKey processKey = new ProcessKey(processInstanceInfo.getId(), processInstanceInfo.getEventTypes(), null);
		map.put(processKey, processInstanceInfo);
		TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
		return processInstanceInfo;
	}

	@Override
	public PersistentCorrelationKey persist(PersistentCorrelationKey correlationKeyInfo) {
		long processInstanceId = correlationKeyInfo.getProcessInstanceId();
		ConcurrentNavigableMap<ProcessKey, PersistentProcessInstance> subMap = map.prefixSubMap(new ProcessKey(processInstanceId, (String[]) null, null));
		if (!subMap.isEmpty()) {
			Map.Entry<ProcessKey, PersistentProcessInstance> entry = subMap.entrySet().iterator().next();
			ProcessKey newKey = new ProcessKey(entry.getKey().getProcessInstanceId(), entry.getKey().getTypes(), correlationKeyInfo);
			PersistentProcessInstance instance = entry.getValue();
			map.remove(entry.getKey());
			map.put(newKey, instance);
		}
		//mapByCK.put(correlationKeyInfo, processInstanceId);
		return correlationKeyInfo;
	}

	@Override
	public PersistentProcessInstance findProcessInstanceInfo(Long processId) {
		try {
			ConcurrentNavigableMap<ProcessKey, PersistentProcessInstance> subMap = map.prefixSubMap(new ProcessKey(processId, (String[]) null, null));
			if (subMap.isEmpty()) {
				return null;
			}
			if (subMap.size() > 1) {
				throw new IllegalArgumentException("Shouldn't have more than one process instance for processId " + processId);
			}

			PersistentProcessInstance inst = subMap.values().iterator().next();
			/*if (!mapById.containsKey(processId)) {
				return null;
			}
			PersistentProcessInstance inst = mapById.get(processId);
			if (((MapDBProcessInstance)inst).isDeleted()) {
				return null;
			}*/
			TransactionManagerHelper.addToUpdatableSet(txm, inst);
			return inst;
		} catch (Throwable t) {
			return null;
		}
	}

	@Override
	public void remove(PersistentProcessInstance processInstanceInfo) {
		TransactionManagerHelper.removeFromUpdatableSet(txm, processInstanceInfo);
		ConcurrentNavigableMap<ProcessKey, PersistentProcessInstance> subMap = map.prefixSubMap(new ProcessKey(processInstanceInfo.getId(), (String[]) null, null));
		List<ProcessKey> keys = new ArrayList<>(subMap.keySet());
		for (ProcessKey key : keys) {
			map.remove(key);
		}
		//TransactionManagerHelper.removeFromUpdatableSet(txm, processInstanceInfo);
		//mapById.remove(processInstanceInfo.getId());//, processInstanceInfo);
	}

	@Override
	public List<Long> getProcessInstancesWaitingForEvent(String type) {
		/*if (!mapByEventTypes.containsKey(type)) {
			return new ArrayList<>();
		}
		long[] values = mapByEventTypes.get(type);
		List<Long> retval = new ArrayList<>();
		for (long value : values) {
			retval.add(value);
		}
		return retval;*/
		ConcurrentNavigableMap<ProcessKey, PersistentProcessInstance> subMap = map.subMap(
				new ProcessKey(Long.MIN_VALUE, new String[] {type}, null),
				new ProcessKey(Long.MAX_VALUE, new String[] {type}, null));
		List<Long> retval = new ArrayList<>();
		for (ProcessKey key : subMap.keySet()) {
			retval.add(key.getProcessInstanceId());
		}
		return retval;
	}

	@Override
	public void close() {
		super.close();
		//map.close();
	}
	
	@Override
	public Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
		//return mapByCK.getOrDefault(correlationKey, null);
		ConcurrentNavigableMap<ProcessKey, PersistentProcessInstance> subMap = map.subMap(
				new ProcessKey(Long.MIN_VALUE, (String[]) null, correlationKey),
				new ProcessKey(Long.MAX_VALUE, (String[]) null, correlationKey));
		if (subMap.isEmpty()) {
			return null;
		}
		if (subMap.size() > 1) {
			throw new IllegalArgumentException("Shouldn't have more than one process instance for correlation key");
		}
		return subMap.keySet().iterator().next().getProcessInstanceId();
	}
}
