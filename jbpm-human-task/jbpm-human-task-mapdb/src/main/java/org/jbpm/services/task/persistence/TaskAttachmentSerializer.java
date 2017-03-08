package org.jbpm.services.task.persistence;

import java.io.IOException;
import java.util.Date;

import org.jbpm.services.task.impl.model.AttachmentImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.Attachment;
import org.kie.internal.task.api.model.AccessType;
import org.kie.internal.task.api.model.InternalAttachment;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskAttachmentSerializer extends
		GroupSerializerObjectArray<Attachment> {

	@Override
	public void serialize(DataOutput2 out, Attachment value) throws IOException {
		InternalAttachment at = (InternalAttachment) value;
		out.writeLong(at.getId());
		out.writeBoolean(at.getAccessType() != null);
		if (at.getAccessType() != null) {
			out.writeUTF(at.getAccessType().name());
		}
		out.writeBoolean(at.getAttachedAt() != null);
		if (at.getAttachedAt() != null) {
			out.writeLong(at.getAttachedAt().getTime());
		}
		out.writeBoolean(at.getAttachedBy() != null);
		if (at.getAttachedBy() != null) {
			out.writeUTF(at.getAttachedBy().getId());
		}
		out.writeLong(at.getAttachmentContentId());
		out.writeBoolean(at.getContentType() != null);
		if (at.getContentType() != null) {
			out.writeUTF(at.getContentType());
		}
		out.writeBoolean(at.getName() != null);
		if (at.getName() != null) {
			out.writeUTF(at.getName());
		}
		out.writeInt(at.getSize());
	}

	@Override
	public Attachment deserialize(DataInput2 input, int available)
			throws IOException {
		AttachmentImpl at = new AttachmentImpl();
		at.setId(input.readLong());
		if (input.readBoolean()) {
			at.setAccessType(AccessType.valueOf(input.readUTF()));
		}
		if (input.readBoolean()) {
			at.setAttachedAt(new Date(input.readLong()));
		}
		if (input.readBoolean()) {
			at.setAttachedBy(new UserImpl(input.readUTF()));
		}
		at.setAttachmentContentId(input.readLong());
		if (input.readBoolean()) {
			at.setContentType(input.readUTF());
		}
		if (input.readBoolean()) {
			at.setName(input.readUTF());
		}
		at.setSize(input.readInt());
		return at;
	}

	@Override
	public int compare(Attachment o1, Attachment o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
