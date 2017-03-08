package org.jbpm.services.task.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.impl.model.AttachmentImpl;
import org.jbpm.services.task.impl.model.BooleanExpressionImpl;
import org.jbpm.services.task.impl.model.CommentImpl;
import org.jbpm.services.task.impl.model.DeadlineImpl;
import org.jbpm.services.task.impl.model.DeadlinesImpl;
import org.jbpm.services.task.impl.model.DelegationImpl;
import org.jbpm.services.task.impl.model.EmailNotificationHeaderImpl;
import org.jbpm.services.task.impl.model.EmailNotificationImpl;
import org.jbpm.services.task.impl.model.EscalationImpl;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.I18NTextImpl;
import org.jbpm.services.task.impl.model.LanguageImpl;
import org.jbpm.services.task.impl.model.NotificationImpl;
import org.jbpm.services.task.impl.model.PeopleAssignmentsImpl;
import org.jbpm.services.task.impl.model.ReassignmentImpl;
import org.jbpm.services.task.impl.model.TaskDataImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.AccessType;
import org.kie.internal.task.api.model.AllowedToDelegate;
import org.kie.internal.task.api.model.BooleanExpression;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Delegation;
import org.kie.internal.task.api.model.EmailNotification;
import org.kie.internal.task.api.model.EmailNotificationHeader;
import org.kie.internal.task.api.model.Escalation;
import org.kie.internal.task.api.model.InternalAttachment;
import org.kie.internal.task.api.model.InternalComment;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.internal.task.api.model.Language;
import org.kie.internal.task.api.model.Notification;
import org.kie.internal.task.api.model.NotificationType;
import org.kie.internal.task.api.model.Reassignment;
import org.kie.internal.task.api.model.SubTasksStrategy;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class TaskSerializer extends GroupSerializerObjectArray<Task> {

	private static final byte LONG_VALUE = (byte) 1;
	private static final byte INT_VALUE = (byte) 2;
	private static final byte STRING_VALUE = (byte) 3;
	
	
	@Override
	public void serialize(DataOutput2 out, Task value) throws IOException {
		InternalTask task = (InternalTask) value;
		if (task != null) {
			if (task.getDeadlines() != null) {
				writeDeadlines("startDeadlines", out, task.getDeadlines().getStartDeadlines());
				writeDeadlines("endDeadlines", out, task.getDeadlines().getEndDeadlines());
			}
			Delegation del = task.getDelegation();
			writeStringEntry("delegation.allowed", out, del == null ? null : del.getAllowed() == null ? null : del.getAllowed().name());
			writeOrgEntities("delegation.delegates", out, del == null ? null : del.getDelegates());
			writeI18NTexts("descriptions", out, task.getDescriptions());
			writeStringEntry("description", out, task.getDescription());
			writeStringEntry("formName", out, task.getFormName());
			writeLongEntry("id", out, task.getId());
			writeStringEntry("name", out, task.getName());
			writeI18NTexts("names", out, task.getNames());
			writePeopleAssignments("peopleAssignments", out, (InternalPeopleAssignments) task.getPeopleAssignments());
			writeIntEntry("priority", out, task.getPriority());
			writeStringEntry("subject", out, task.getSubject());
			writeI18NTexts("subjects", out, task.getSubjects());
			writeStringEntry("subTaskStrategy", out, task.getSubTaskStrategy() == null ? null : task.getSubTaskStrategy().name());
			writeTaskData("taskData", out, (InternalTaskData) task.getTaskData());
			writeStringEntry("taskType", out, task.getTaskType());
		}
		out.writeBoolean(false);
	}
	
	@Override
	public Task deserialize(DataInput2 input, int available) throws IOException {
		TaskImpl task = new TaskImpl();
		Map<String, Object> data = readFully(input);
		DeadlinesImpl deadlines = new DeadlinesImpl();
		deadlines.setStartDeadlines(readDeadlines("startDeadlines", data));
		deadlines.setEndDeadlines(readDeadlines("endDeadlines", data));
		task.setDeadlines(deadlines);
		DelegationImpl del = new DelegationImpl();
		del.setAllowed(data.get("delegation.allowed") == null ? null : AllowedToDelegate.valueOf(data.get("delegation.allowed").toString()));
		del.setDelegates(readOrgEntities("delegation.delegates", data));
		task.setDelegation(del);
		task.setDescriptions(readI18NTexts("descriptions", data));
		task.setDescription((String) data.get("description"));
		task.setFormName((String) data.get("formName"));
		Long l = (Long) data.get("id");
		if (l != null) {
			task.setId(l);
		}
		task.setName((String) data.get("name"));
		task.setNames(readI18NTexts("names", data));
		task.setPeopleAssignments(readPeopleAssignments("peopleAssignments", data));
		Integer i = (Integer) data.get("priority");
		if (i != null) {
			task.setPriority(i);
		}
		task.setSubject((String) data.get("subject"));
		task.setSubjects(readI18NTexts("subjects", data));
		String s = (String) data.get("subTaskStrategy");
		if (s != null) {
			task.setSubTaskStrategy(SubTasksStrategy.valueOf(s));
		}
		task.setTaskData(readTaskData("taskData", data));
		task.setTaskType((String) data.get("taskType"));
		return task;
	}

	private void writeTaskData(String key, DataOutput2 out, InternalTaskData data) throws IOException {
		writeDate(key + ".activationTime", out, data.getActivationTime());
		writeOrgEntity(key + ".actualOwner", out, data.getActualOwner());
		writeAttachments(key + ".attachments", out, data.getAttachments());
		writeComments(key + ".comments", out, data.getComments());
		writeOrgEntity(key + ".createdBy", out, data.getCreatedBy());
		writeDate(key + ".createdOn", out, data.getCreatedOn());
		writeStringEntry(key + ".deploymentId", out, data.getDeploymentId());
		writeStringEntry(key + ".documentAccessType", out, data.getDocumentAccessType() == null ? null : data.getDocumentAccessType().name());
		writeLongEntry(key + ".documentContentId", out, data.getDocumentContentId());
		writeStringEntry(key + ".documentType", out, data.getDocumentType());
		writeDate(key + ".expirationTime", out, data.getExpirationTime());
		writeStringEntry(key + ".faultAccessType", out, data.getFaultAccessType() == null ? null : data.getFaultAccessType().name());
		writeLongEntry(key + ".faultContentId", out, data.getFaultContentId());
		writeStringEntry(key + ".faultName", out, data.getFaultName());
		writeStringEntry(key + ".faultType", out, data.getFaultType());
		writeStringEntry(key + ".outputAccessType", out, data.getOutputAccessType() == null ? null : data.getOutputAccessType().name());
		writeLongEntry(key + ".outputContentId", out, data.getOutputContentId());
		writeStringEntry(key + ".outputType", out, data.getOutputType());
		writeLongEntry(key + ".parentId", out, data.getParentId());
		writeStringEntry(key + ".previousStatus", out, data.getPreviousStatus() == null ? null : data.getPreviousStatus().name());
		writeStringEntry(key + ".processId", out, data.getProcessId());
		writeLongEntry(key + ".processInstanceId", out, data.getProcessInstanceId());
		writeLongEntry(key + ".processSessionId", out, data.getProcessSessionId());
		writeStringEntry(key + ".status", out, data.getStatus() == null ? null : data.getStatus().name());
		writeLongEntry(key + ".workItemId", out, data.getWorkItemId());
		writeIntEntry(key + ".skippable", out, data.isSkipable() ? 1 : 0);
	}

	private TaskData readTaskData(String prefix, Map<String, Object> data) {
		TaskDataImpl retval = new TaskDataImpl();
		Long l = (Long) data.get(prefix + ".activationTime");
		if (l != null) retval.setActivationTime(new Date(l));
		retval.setActualOwner((User) readOrgEntity(prefix + ".actualOwner", data));
		retval.setAttachments(readAttachments(prefix + ".attachments", data));
		retval.setComments(readComments(prefix + ".comments", data));
		retval.setCreatedBy((User) readOrgEntity(prefix + ".createdBy", data));
		l = (Long) data.get(prefix + ".createdOn");
		if (l != null) retval.setCreatedOn(new Date(l));
		retval.setDeploymentId((String) data.get(prefix + ".deploymentId"));
		String s = (String) data.get(prefix + ".documentAccessType");
		if (s != null) retval.setDocumentAccessType(AccessType.valueOf(s));
		l = (Long) data.get(prefix + ".documentContentId");
		if (l != null) retval.setDocumentContentId(l);
		retval.setDocumentType((String) data.get(prefix + ".documentType"));
		l = (Long) data.get(prefix + ".expirationTime");
		if (l != null) retval.setExpirationTime(new Date(l));
		s = (String) data.get(prefix + ".faultAccessType");
		if (s != null) retval.setFaultAccessType(AccessType.valueOf(s));
		l = (Long) data.get(prefix + ".faultContentId");
		if (l != null) retval.setFaultContentId(l);
		retval.setFaultName((String) data.get(prefix + ".faultName")); 
		retval.setFaultType((String) data.get(prefix + ".faultType"));
		s = (String) data.get(prefix + ".outputAccessType");
		if (s != null) retval.setOutputAccessType(AccessType.valueOf(s));
		l = (Long) data.get(prefix + ".outputContentId");
		if (l != null) retval.setOutputContentId(l);
		retval.setOutputType((String) data.get(prefix + ".outputType"));
		l = (Long) data.get(prefix + ".parentId");
		if (l != null) retval.setParentId(l);
		retval.setProcessId((String) data.get(prefix + ".processId"));
		l = (Long) data.get(prefix + ".processInstanceId");
		if (l != null) retval.setProcessInstanceId(l);
		l = (Long) data.get(prefix + ".processSessionId");
		if (l != null) retval.setProcessSessionId(l);
		s = (String) data.get(prefix + ".status");
		if (s != null) retval.setStatus(Status.valueOf(s));
		s = (String) data.get(prefix + ".previousStatus");
		if (s != null) { 
			retval.setPreviousStatus(Status.valueOf(s));
		} else {
			retval.setPreviousStatus(null);
		}
		l = (Long) data.get(prefix + ".workItemId");
		if (l != null) retval.setWorkItemId(l);
		Integer i = (Integer) data.get(prefix + ".skippable");
		if (i != null) {
			retval.setSkipable(i > 0);
		}
		return retval;
	}

	private void writeComments(String key, DataOutput2 out, List<Comment> comments) throws IOException {
		if (comments != null) {
			writeIntEntry(key + ".size", out, comments.size());
			for (int index = 0; index < comments.size(); index++) {
				InternalComment ic = (InternalComment) comments.get(index);
				writeDate(key + "[" + index + "].addedAt", out, ic.getAddedAt());
				writeOrgEntity(key + "[" + index + "].addedBy", out, ic.getAddedBy());
				writeLongEntry(key + "[" + index + "].id", out, ic.getId());
				writeStringEntry(key + "[" + index + "].text", out, ic.getText());
			}
		}
	}

	private List<Comment> readComments(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<Comment> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			CommentImpl ic = new CommentImpl();
			Long l = (Long) data.get(prefix + "[" + index + "].addedAt");
			if (l != null) ic.setAddedAt(new Date(l));
			ic.setAddedBy((User) readOrgEntity(prefix + "[" + index + "].addedBy", data));
			l = (Long) data.get(prefix + "[" + index + "].id");
			if (l != null) ic.setId(l);
			ic.setText((String) data.get(prefix + "[" + index + "].text"));
			retval.add(ic);
		}
		return retval;
	}

	private void writeAttachments(String key, DataOutput2 out, List<Attachment> attachments) throws IOException {
		if (attachments != null) {
			writeIntEntry(key + ".size", out, attachments.size());
			for (int index = 0; index < attachments.size(); index++) {
				InternalAttachment iat = (InternalAttachment) attachments.get(index);
				writeStringEntry(key + "[" + index + "].accessType", out, iat.getAccessType() == null ? null : iat.getAccessType().name());
				writeDate(key + "[" + index + "].attachedAt", out, iat.getAttachedAt());
				writeOrgEntity(key + "[" + index + "].attachedBy", out, iat.getAttachedBy());
				writeLongEntry(key + "[" + index + "].attachmentContentId", out, iat.getAttachmentContentId());
				writeStringEntry(key + "[" + index + "].contentType", out, iat.getContentType());
				writeLongEntry(key + "[" + index + "].id", out, iat.getId());
				writeStringEntry(key + "[" + index + "].name", out, iat.getName());
				writeIntEntry(key + "[" + index + "].size", out, iat.getSize());
			}
		}
	}
	
	private List<Attachment> readAttachments(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<Attachment> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			AttachmentImpl iat = new AttachmentImpl();
			String s = (String) data.get(prefix + "[" + index + "].accessType");
			if (s != null) iat.setAccessType(AccessType.valueOf(s));
			Long l = (Long) data.get(prefix + "[" + index + "].attachedAt");
			if (l != null) iat.setAttachedAt(new Date(l));
			iat.setAttachedBy((User) readOrgEntity(prefix + "[" + index + "].attachedBy", data));
			l = (Long) data.get(prefix + "[" + index + "].attachmentContentId");
			if (l != null) iat.setAttachmentContentId(l);
			iat.setContentType((String) data.get(prefix + "[" + index + "].contentType"));
			l = (Long) data.get(prefix + "[" + index + "].id");
			if (l != null) iat.setId(l);
			iat.setName((String) data.get(prefix + "[" + index + "].name"));
			Integer i = (Integer) data.get(prefix + "[" + index + "].size");
			if (i != null) iat.setSize(i);
			retval.add(iat);
		}
		return retval;
	}

	private static void writeDate(String key, DataOutput2 out, Date date) throws IOException{
		writeLongEntry(key, out, date == null ? null : date.getTime());
	}
	
	private static void writeOrgEntity(String key, DataOutput2 out, OrganizationalEntity entity) throws IOException {
		writeStringEntry(key + ".type", out, entity == null ? null : entity instanceof User ? "user" : "group");
		writeStringEntry(key + ".id", out, entity == null ? null : entity.getId());
	}

	private static OrganizationalEntity readOrgEntity(String prefix, Map<String, Object> data) {
		String type = (String) data.get(prefix + ".type");
		if ("user".equals(type)) {
			return new UserImpl((String) data.get(prefix + ".id"));
		} else if ("group".equals(type)) {
			return new GroupImpl((String) data.get(prefix + ".id"));
		} else {
			return null;
		}
	}

	private static void writeI18NTexts(String key, DataOutput2 out, List<I18NText> texts) throws IOException {
		if (texts != null) {
			writeIntEntry(key + ".size", out, texts.size());
			for (int index = 0; index < texts.size(); index++) {
				I18NText txt = texts.get(index);
				writeLongEntry(key + "[" + index + "].id", out, txt.getId());
				writeStringEntry(key + "[" + index + "].language", out, txt.getLanguage());
				writeStringEntry(key + "[" + index + "].text", out, txt.getText());
			}
		}
	}
	
	private static List<I18NText> readI18NTexts(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<I18NText> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			I18NTextImpl txt = new I18NTextImpl();
			txt.setId((Long) data.get(prefix + "[" + index + "].id"));
			txt.setLanguage((String) data.get(prefix + "[" + index + "].language"));
			txt.setText((String) data.get(prefix + "[" + index + "].text"));
			retval.add(txt);
		}
		return retval;
	}
	
	private static void writeOrgEntities(String key, DataOutput2 out, List<OrganizationalEntity> entities) throws IOException {
		if (entities != null) {
			writeIntEntry(key + ".size", out, entities.size());
			for (int index = 0; index < entities.size(); index++) { 
				OrganizationalEntity entity = entities.get(index);
				writeOrgEntity(key + "[" + index + "]", out, entity);
			}
		}
	}
	
	private static List<OrganizationalEntity> readOrgEntities(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<OrganizationalEntity> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			retval.add(readOrgEntity(prefix + "[" + index + "]", data));
		}
		return retval;
	}
	
	private void writeDeadlines(String key, DataOutput2 out, List<Deadline> deadlines) throws IOException {
		if (deadlines != null) {
			writeIntEntry(key + ".size", out, deadlines.size());
			for (int index = 0; index < deadlines.size(); index++) {
				Deadline d = deadlines.get(index);
				writeDeadline(key + "[" + index + "]", out, d);
			}
		}
	}
	
	public static List<Deadline> readDeadlines(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<Deadline> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			retval.add(readDeadline(prefix + "[" + index + "]", data));
		}
		return retval;
	}

	public static Deadline readDeadline(String prefix, Map<String, Object> data) {
		DeadlineImpl retval = new DeadlineImpl();
		Long date = (Long) data.get(prefix + ".date");
		if (date != null) {
			retval.setDate(new Date(date));
		}
		retval.setDocumentation(readI18NTexts(prefix + ".documentation", data));
		Integer size = (Integer) data.get(prefix + ".escalations.size");
		if (size != null) {
			List<Escalation> escs = new ArrayList<>(size);
			for (int index = 0; index < size; index++) {
				EscalationImpl esc = new EscalationImpl();
				esc.setConstraints(readExpressions(prefix + ".escalations[" + index + "].constraints", data));
				Long escId = (Long) data.get(prefix + ".escalations[" + index + "].id");
				if (escId != null) {
					esc.setId(escId);
				}
				esc.setName((String) data.get(prefix + ".escalations[" + index + "].name"));
				esc.setNotifications(readNotifications(prefix + ".escalations[" + index + "].notifications", data));
				esc.setReassignments(readReassignments(prefix + ".escalations[" + index + "].reassignments", data));
				escs.add(esc);
			}
			retval.setEscalations(escs);
		}
		Long id = (Long) data.get(prefix + ".id");
		if (id != null) {
			retval.setId(id);
		}
		return retval;
	}

	public static void writeDeadline(String key, DataOutput2 out, Deadline d) throws IOException {
		writeDate(key + ".date", out, d.getDate());
		writeI18NTexts(key + ".documentation", out, d.getDocumentation());
		if (d.getEscalations() != null) {
			writeIntEntry(key + ".escalations.size", out, d.getEscalations().size());
			for (int index = 0; index < d.getEscalations().size(); index++) {
				Escalation esc = d.getEscalations().get(index);
				writeExpressions(key + ".escalations[" + index + "].constraints", out, esc.getConstraints());
				writeLongEntry(key + ".escalations[" + index + "].id", out, esc.getId());
				writeStringEntry(key + ".escalations[" + index + "].name", out, esc.getName());
				writeNotifications(key + ".escalations[" + index + "].notifications", out, esc.getNotifications());
				writeReassignments(key + ".escalations[" + index + "].reassignments", out, esc.getReassignments());
			}
		}
		writeLongEntry(key + ".id", out, d.getId());
	}

	private static void writeReassignments(String key, DataOutput2 out, List<Reassignment> reassignments) throws IOException {
		if (reassignments != null) {
			writeIntEntry(key + ".size", out, reassignments.size());
			for (int index = 0; index < reassignments.size(); index++) {
				Reassignment rea = (Reassignment) reassignments.get(index);
				writeI18NTexts(key + "[" + index + "].documentation", out, rea.getDocumentation());
				writeLongEntry(key + "[" + index + "].id", out, rea.getId());
				writeOrgEntities(key + "[" + index + "].potentialOwners", out, rea.getPotentialOwners());
			}
		}
	}

	private static List<Reassignment> readReassignments(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<Reassignment> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			ReassignmentImpl rea = new ReassignmentImpl();
			rea.setDocumentation(readI18NTexts(prefix + "[" + index + "].documentation", data));
			Long id = (Long) data.get(prefix + "[" + index + "].id");
			if (id != null) {
				rea.setId(id);
			}
			rea.setPotentialOwners(readOrgEntities(prefix + "[" + index + "].potentialOwners", data));
			retval.add(rea);
		}
		return retval;
	}

	private static void writeNotifications(String key, DataOutput2 out, List<Notification> nots) throws IOException {
		if (nots != null) {
			writeIntEntry(key + ".size", out, nots.size());
			for (int index = 0; index < nots.size(); index++) {
				Notification not = nots.get(index);
				writeOrgEntities(key + "[" + index + "].businessAdministrators", out, not.getBusinessAdministrators());
				writeI18NTexts(key + "[" + index + "].descriptions", out, not.getDescriptions());
				writeI18NTexts(key + "[" + index + "].documentation", out, not.getDocumentation());
				writeLongEntry(key + "[" + index + "].id", out, not.getId());
				writeStringEntry(key + "[" + index + "].notificationType", out, not.getNotificationType() == null ? null : not.getNotificationType().name());
				writeI18NTexts(key + "[" + index + "].names", out, not.getNames());
				writeIntEntry(key + "[" + index + "].priority", out, not.getPriority());
				writeOrgEntities(key + "[" + index + "].recipients", out, not.getRecipients());
				writeI18NTexts(key + "[" + index + "].subjects", out, not.getSubjects());
				if (not instanceof EmailNotification) {
					writeEmailHeaders(key + "[" + index + "].emailHeaders", out, ((EmailNotification) not).getEmailHeaders());
				}
			}
		}
	}
	
	private static List<Notification> readNotifications(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<Notification> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			NotificationImpl not = new NotificationImpl();
			if (data.get(prefix+ "[" + index + "].notificationType") != null) {
				NotificationType type = NotificationType.valueOf((String) data.get(prefix+ "[" + index + "].notificationType"));
				if (type == NotificationType.Email) {
					not = new EmailNotificationImpl();
				}
			}
			not.setBusinessAdministrators(readOrgEntities(prefix + "[" + index + "].businessAdministrators", data));
			not.setDescriptions(readI18NTexts(prefix+ "[" + index + "].descriptions", data));
			not.setDocumentation(readI18NTexts(prefix + "[" + index + "].documentation", data));
			Long id = (Long) data.get(prefix + "[" + index + "].id");
			if (id != null) {
				not.setId(id);
			}
			not.setNames(readI18NTexts(prefix + "[" + index + "].names", data));
			not.setPriority((Integer) data.get(prefix + "[" + index + "].priority"));
			not.setRecipients(readOrgEntities(prefix + "[" + index + "].recipients", data));
			not.setSubjects(readI18NTexts(prefix + "[" + index + "].subjects", data));
			if (not instanceof EmailNotificationImpl) {
				EmailNotificationImpl emnot = (EmailNotificationImpl) not;
				emnot.setEmailHeaders(readEmailHeaders(prefix + "[" + index + "].emailHeaders", data));
			}
			retval.add(not);
		}
		return retval;
	}

	private static void writeEmailHeaders(String key, DataOutput2 out,
			Map<? extends Language, ? extends EmailNotificationHeader> emailHeaders) throws IOException {
		if (emailHeaders == null) {
			return;
		}
		StringBuilder keysString = new StringBuilder();
		for (Iterator<? extends Language> iter = emailHeaders.keySet().iterator(); iter.hasNext(); ) {
			Language l = iter.next();
			keysString.append(l.getMapkey());
			if (iter.hasNext()) {
				keysString.append(",");
			}
		}
		writeStringEntry(key + ".keys", out, keysString.toString());
		for (Map.Entry<? extends Language, ? extends EmailNotificationHeader> entry : emailHeaders.entrySet()) {
			String lang = entry.getKey().getMapkey();
			EmailNotificationHeader header = entry.getValue();
			writeStringEntry(key + "[" + lang + "].body", out, header.getBody());
			writeStringEntry(key + "[" + lang + "].from", out, header.getFrom());
			writeLongEntry(key + "[" + lang + "].id", out, header.getId());
			writeStringEntry(key + "[" + lang + "].language", out, header.getLanguage());
			writeStringEntry(key + "[" + lang + "].replyTo", out, header.getReplyTo());
			writeStringEntry(key + "[" + lang + "].subject", out, header.getSubject());
		}
	}

	
	private static Map<Language, EmailNotificationHeader> readEmailHeaders(
			String prefix, Map<String, Object> data) {
		Map<Language, EmailNotificationHeader> retval = new HashMap<>();
		String keys = (String) data.get(prefix + ".keys");
		if (keys == null) {
			return retval;
		}
		String[] actualKeys = keys.split(",");
		for (String key : actualKeys) {
			Language lang = new LanguageImpl(key);
			EmailNotificationHeaderImpl header = new EmailNotificationHeaderImpl();
			header.setBody((String) data.get(prefix + "[" + key + "].body"));
			header.setFrom((String) data.get(prefix + "[" + key + "].from"));
			header.setId((Long) data.get(prefix + "[" + key + "].id"));
			header.setLanguage((String) data.get(prefix + "[" + key + "].language"));
			header.setReplyTo((String) (String) data.get(prefix + "[" + key + "].replyTo"));
			header.setSubject((String) (String) data.get(prefix + "[" + key + "].subject"));
			retval.put(lang, header);
		}
		return retval;
	}

	private static void writeExpressions(String key, DataOutput2 out, List<BooleanExpression> exprs) throws IOException {
		if (exprs != null) {
			writeIntEntry(key + ".size", out, exprs.size());
			for (int index = 0; index < exprs.size(); index++) {
				BooleanExpression exp = exprs.get(index);
				writeStringEntry(key + "[" + index + "].expression", out, exp.getExpression());
				writeStringEntry(key + "[" + index + "].type", out, exp.getType());
				writeLongEntry(key + "[" + index + "].id", out, exp.getId());
			}
		}
	}
	
	private static List<BooleanExpression> readExpressions(String prefix, Map<String, Object> data) {
		Integer size = (Integer) data.get(prefix + ".size");
		if (size == null) {
			return null;
		}
		List<BooleanExpression> retval = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			BooleanExpressionImpl exp = new BooleanExpressionImpl();
			exp.setExpression((String) data.get(prefix + "[" + index + "].expression"));
			exp.setType((String) data.get(prefix + "[" + index + "].type"));
			Long id = (Long) data.get(prefix + "[" + index + "].id");
			if (id != null) {
				exp.setId(id);
			}
			retval.add(exp);
		}
		return retval;
	}

	private void writePeopleAssignments(String key, DataOutput2 out, InternalPeopleAssignments assignments) throws IOException {
		writeOrgEntities(key + ".businessAdministrators", out, assignments.getBusinessAdministrators());
		writeOrgEntities(key + ".excludedOwners", out, assignments.getExcludedOwners());
		writeOrgEntities(key + ".potentialOwners", out, assignments.getPotentialOwners());
		writeOrgEntities(key + ".recipients", out, assignments.getRecipients());
		writeOrgEntity(key + ".taskInitiator", out, assignments.getTaskInitiator());
		writeOrgEntities(key + ".taskStakeholders", out, assignments.getTaskStakeholders());
	}

	private PeopleAssignments readPeopleAssignments(String prefix, Map<String, Object> data) {
		PeopleAssignmentsImpl retval = new PeopleAssignmentsImpl();
		retval.setBusinessAdministrators(readOrgEntities(prefix + ".businessAdministrators", data));
		retval.setExcludedOwners(readOrgEntities(prefix + ".excludedOwners", data));
		retval.setPotentialOwners(readOrgEntities(prefix + ".potentialOwners", data));
		retval.setRecipients(readOrgEntities(prefix + ".recipients", data));		
		retval.setTaskInitiator((User) readOrgEntity(prefix + ".taskInitiator", data));
		retval.setTaskStakeholders(readOrgEntities(prefix + ".taskStakeholders", data));
		return retval;
	}

	@Override
	public int compare(Task o1, Task o2) {
		return o1.getId().compareTo(o2.getId());
	}

	private static void writeLongEntry(String key, DataOutput2 out, Long value) throws IOException {
		out.writeBoolean(true);
		out.writeUTF(key);
		out.write(LONG_VALUE);
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeLong(value);
		}
	}

	private static void writeIntEntry(String key, DataOutput2 out, Integer value) throws IOException {
		out.writeBoolean(true);
		out.writeUTF(key);
		out.write(INT_VALUE);
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeInt(value);
		}
	}

	private static void writeStringEntry(String key, DataOutput2 out, String value) throws IOException {
		out.writeBoolean(true);
		out.writeUTF(key);
		out.write(STRING_VALUE);
		out.writeBoolean(value != null);
		if (value != null) {
			out.writeInt(value.length());
			for (int index = 0; index < value.length(); index++) {
				out.writeChar(value.charAt(index));
			}
		}
	}

	public static Map<String, Object> readFully(DataInput2 input) throws IOException {
		Map<String, Object> retval = new HashMap<>();
		while (input.readBoolean()) {
			String key = input.readUTF();
			byte type = input.readByte();
			switch (type) {
			case LONG_VALUE:
				Long l = null;
				if (input.readBoolean()) {
					l = input.readLong();
				}
				retval.put(key, l);
				break;
			case INT_VALUE:
				Integer i = null;
				if (input.readBoolean()) {
					i = input.readInt();
				}
				retval.put(key, i);
				break;
			case STRING_VALUE:
				String s = null;
				if (input.readBoolean()) {
					int length = input.readInt();
					StringBuilder sb = new StringBuilder();
					for (int index = 0; index < length; index++) {
						sb.append(input.readChar());
					}
					s = sb.toString();
				}
				retval.put(key, s);
				break;
			}
		}
		return retval;
	}
}
