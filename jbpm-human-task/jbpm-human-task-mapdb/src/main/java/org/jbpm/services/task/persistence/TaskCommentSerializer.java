package org.jbpm.services.task.persistence;

import java.io.IOException;
import java.util.Date;

import org.jbpm.services.task.impl.model.CommentImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.Comment;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskCommentSerializer extends GroupSerializerObjectArray<Comment> {

	@Override
	public void serialize(DataOutput2 out, Comment value) throws IOException {
		out.writeLong(value.getId());
		out.writeBoolean(value.getText() != null);
		if (value.getText() != null) {
			out.writeUTF(value.getText());
		}
		out.writeBoolean(value.getAddedAt() != null);
		if (value.getAddedAt() != null) {
			out.writeLong(value.getAddedAt().getTime());
		}
		out.writeBoolean(value.getAddedBy() != null);
		if (value.getAddedBy() != null) {
			out.writeUTF(value.getAddedBy().getId());
		}
	}

	@Override
	public Comment deserialize(DataInput2 input, int available)
			throws IOException {
		CommentImpl comment = new CommentImpl();
		comment.setId(input.readLong());
		if (input.readBoolean()) {
			comment.setText(input.readUTF());
		}
		if (input.readBoolean()) {
			comment.setAddedAt(new Date(input.readLong()));
		}
		if (input.readBoolean()) {
			comment.setAddedBy(new UserImpl(input.readUTF()));
		}
		return comment;
	}

	@Override
	public int compare(Comment o1, Comment o2) {
		return o1.getId().compareTo(o2.getId());
	}

}
