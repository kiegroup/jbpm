package org.jbpm.services.task.persistence;

import java.io.IOException;

import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class OrganizationalEntitySerializer extends
		GroupSerializerObjectArray<OrganizationalEntity> {

	@Override
	public void serialize(DataOutput2 out, OrganizationalEntity value)
			throws IOException {
		out.writeBoolean(value instanceof User);
		out.writeUTF(value.getId());
	}

	@Override
	public OrganizationalEntity deserialize(DataInput2 input, int available) throws IOException {
		OrganizationalEntity entity = null;
		if (input.readBoolean()) {
			entity = new UserImpl(input.readUTF());
		} else {
			entity = new GroupImpl(input.readUTF());
		}
		return entity;
	}

	@Override
	public int compare(OrganizationalEntity o1, OrganizationalEntity o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
