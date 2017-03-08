package org.jbpm.services.task.persistence;

import java.io.IOException;
import java.util.Map;

import org.kie.internal.task.api.model.Deadline;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskDeadlineSerializer extends GroupSerializerObjectArray<Deadline> {

	@Override
	public void serialize(DataOutput2 out, Deadline value) throws IOException {
		TaskSerializer.writeDeadline("deadline", out, value);
	}

	@Override
	public Deadline deserialize(DataInput2 input, int available) throws IOException {
		Map<String, Object> map = TaskSerializer.readFully(input);
		return TaskSerializer.readDeadline("deadline", map);
	}

	@Override
	public int compare(Deadline o1, Deadline o2) {
		return Long.valueOf(o1.getId()).compareTo(o2.getId());
	}

}
