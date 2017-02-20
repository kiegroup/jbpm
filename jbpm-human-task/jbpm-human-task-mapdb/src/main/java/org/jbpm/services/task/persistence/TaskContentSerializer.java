package org.jbpm.services.task.persistence;

import java.io.IOException;

import org.jbpm.services.task.impl.model.ContentImpl;
import org.kie.api.task.model.Content;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskContentSerializer extends GroupSerializerObjectArray<Content> {

	@Override
	public void serialize(DataOutput2 out, Content value) throws IOException {
		out.writeLong(value.getId());
		out.writeBoolean(value.getContent() != null);
		if (value.getContent() != null) {
			out.writeInt(value.getContent().length);
			out.write(value.getContent());
		}
	}

	@Override
	public Content deserialize(DataInput2 input, int available)
			throws IOException {
		ContentImpl ic = new ContentImpl();
		ic.setId(input.readLong());
		if (input.readBoolean()) {
			byte[] data = new byte[input.readInt()];
			input.readFully(data);
			ic.setContent(data);
		}
		return ic;
	}

	@Override
	public int compare(Content o1, Content o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
