package org.jbpm.services.task.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalPeopleAssignments;

public class TaskKey implements Comparable<TaskKey> {

	private Long taskId;
	private Status[] status;
	private String[] userIds;
	private Long contentId;
	private Long attachmentId;
	private Long commentId;
	private Long deadlineId;
	
	public TaskKey(Task task) {
		this(task.getId());
		List<String> userIdsList = new ArrayList<>();
		InternalPeopleAssignments assignments = (InternalPeopleAssignments) task.getPeopleAssignments();
		userIdsList.addAll(getIds("potowners", assignments.getPotentialOwners()));
		userIdsList.addAll(getIds("bizadmins", assignments.getBusinessAdministrators()));
		userIdsList.addAll(getIds("stakeholders", assignments.getTaskStakeholders()));
		userIdsList.addAll(getIds("recipients", assignments.getRecipients()));
		userIdsList.addAll(getIds("exclowners", assignments.getExcludedOwners()));
		if (task.getPeopleAssignments().getTaskInitiator() != null) {
			userIdsList.add("taskinitiator_" + task.getPeopleAssignments().getTaskInitiator().getId());
		}
		this.status = new Status[] { task.getTaskData().getStatus() };
		this.userIds = userIdsList.toArray(new String[userIdsList.size()]);
	}
	
	public TaskKey(Long taskId, Status[] status, String[] userIds) {
		this.taskId = taskId;
		this.status = status;
		this.userIds = userIds.clone();
	}
	
	private List<String> getIds(String prefix, List<OrganizationalEntity> entities) {
		List<String> retval = new ArrayList<>();
		if (entities != null) {
			for (OrganizationalEntity entity : entities) {
				retval.add(prefix + "_" + entity.getId());
			}
		}
		return retval;
	}

	public TaskKey(Long taskId) {
		this.taskId = taskId;
	}
	
	public TaskKey(Long taskId, Long contentId, Long attachmentId,
			Long commentId, Long deadlineId) {
		super();
		this.taskId = taskId;
		this.contentId = contentId;
		this.attachmentId = attachmentId;
		this.commentId = commentId;
		this.deadlineId = deadlineId;
	}

	@Override
	public int compareTo(TaskKey other) {
		if (other == null) {
			return 1;
		}
		if (other == this) {
			return 0;
		}
		int retval = 0;
		if (other.getContentId() != null && getContentId() != null) {
			retval += getContentId().compareTo(other.getContentId());
		} else if (other.getAttachmentId() != null && getAttachmentId() != null) {
			retval += getAttachmentId().compareTo(other.getAttachmentId());
		} else if (other.getCommentId() != null && getCommentId() != null) {
			retval += getCommentId().compareTo(other.getCommentId());
		} else if (other.getDeadlineId() != null && getDeadlineId() != null) {
			retval += getDeadlineId().compareTo(other.getDeadlineId());
		}
		if (other.getTaskId() != null && getTaskId() != null) {
			retval += (getTaskId().compareTo(other.getTaskId())) * 1000;
		}
		if (this.userIds != null && other.getUserIds() != null) {
			List<String> ours = Arrays.asList(this.userIds);
			List<String> theirs = Arrays.asList(other.getUserIds());
			ours.retainAll(theirs);
			if (ours.size() > 0) {
				retval += ours.size() * 10;
			}
		}
		if (this.status != null && other.getStatus() != null) {
			List<Status> ours = Arrays.asList(this.status);
			List<Status> theirs = Arrays.asList(other.getStatus());
			ours.retainAll(theirs);
			if (ours.size() > 0) {
				retval += ours.size();
			}
		}
		return retval;
	}

	public Long getTaskId() {
		return taskId;
	}
	
	public Long getContentId() {
		return contentId;
	}
	
	public Long getAttachmentId() {
		return attachmentId;
	}
	
	public Long getCommentId() {
		return commentId;
	}
	
	public Long getDeadlineId() {
		return deadlineId;
	}
	
	public String[] getUserIds() {
		return userIds;
	}
	
	public Status[] getStatus() {
		return status;
	}
}
