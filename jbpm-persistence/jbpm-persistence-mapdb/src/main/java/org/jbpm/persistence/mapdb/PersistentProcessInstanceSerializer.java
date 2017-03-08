package org.jbpm.persistence.mapdb;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.persistence.PersistentProcessInstance;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class PersistentProcessInstanceSerializer extends GroupSerializerObjectArray<PersistentProcessInstance> {

	@Override
	public void serialize(DataOutput2 out, PersistentProcessInstance value) throws IOException {
		MapDBProcessInstance inst = (MapDBProcessInstance) value;
		out.writeLong(inst.getId());
		out.writeInt(inst.getState());
		Set<String> eventTypes = inst.getEventTypes() == null ? new HashSet<>() : inst.getEventTypes();
		out.writeInt(eventTypes.size());
		for (String type : eventTypes) {
			out.writeUTF(type);
		}
		out.writeLong(inst.getLastModificationDate() == null ? 0L : inst.getLastModificationDate().getTime());
		out.writeUTF(inst.getProcessId());
		out.writeLong(inst.getStartDate() == null ? 0L : inst.getStartDate().getTime());
		byte[] data = inst.getProcessInstanceByteArray() == null ? new byte[0] : inst.getProcessInstanceByteArray();
		String base64data = new String(Base64.getEncoder().encode(data));
		out.writeUTF(base64data);
	}

	@Override
	public PersistentProcessInstance deserialize(DataInput2 input, int available) throws IOException {
		MapDBProcessInstance inst = new MapDBProcessInstance();
		inst.setId(input.readLong());
		inst.setState(input.readInt());
		int size = input.readInt();
		Set<String> eventTypes = new HashSet<String>();
		for (int index = 0; index < size; index++) {
			eventTypes.add(input.readUTF());
		}
		inst.setEventTypes(eventTypes);
		inst.setLastModificationDate(new Date(input.readLong()));
		inst.setProcessId(input.readUTF());
		inst.setStartDate(new Date(input.readLong()));
		String encodedData = input.readUTF();
		inst.setProcessInstanceByteArray(Base64.getDecoder().decode(encodedData));
		return inst;
	}

	@Override
	public int compare(PersistentProcessInstance p1, PersistentProcessInstance p2) {
		ProcessKey key1 = new ProcessKey(p1.getId(), p1.getEventTypes(), null);
		ProcessKey key2 = new ProcessKey(p2.getId(), p2.getEventTypes(), null);
		return key1.compareTo(key2);
	}

}
