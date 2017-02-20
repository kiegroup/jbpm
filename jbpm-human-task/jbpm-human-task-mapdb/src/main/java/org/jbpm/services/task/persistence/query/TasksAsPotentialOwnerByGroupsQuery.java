package org.jbpm.services.task.persistence.query;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

public class TasksAsPotentialOwnerByGroupsQuery implements MapDBQuery<List<Object[]>> {

	private final boolean optional;
	
	
	public TasksAsPotentialOwnerByGroupsQuery(boolean optional) {
		super();
		this.optional = optional;
	}

	/* select t.id, potentialOwners.id
       from TaskImpl t join t.peopleAssignments.potentialOwners potentialOwners        
       where t.archived = 0 and t.taskData.actualOwner = null and          
       t.taskData.status in ('Created', 'Ready', 'Reserved', 'InProgress', 'Suspended') and  
       potentialOwners.id in (:groupIds)
     */
	@Override
	public List<Object[]> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Date expDate = (Date) params.get("expirationDate");
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		if (status == null) {
			status = Arrays.asList(
				Status.Created, Status.Ready, Status.Reserved, 
				Status.InProgress, Status.Suspended);
		}
		Set<Long> values = new HashSet<>();
		for (String groupId : groupIds) {
			MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), groupId);
		}
		Set<Long> valuesByStatus = new HashSet<>();
		if (status != null) {
            List<String> strStatus = MapDBQueryUtil.asStringStatus(status);
		    for (Long value : values) {
			    String taskStatus = tts.getTaskStatusById().get(value);
			    if (taskStatus != null && strStatus.contains(taskStatus)) {
				    valuesByStatus.add(value);
			    }
		    }
		    values.retainAll(valuesByStatus); //and operation
		}
		
		cleanTasksWithActualOwners(values, tts.getById());
		
		List<Object[]> retval = new LinkedList<>();
		
		for (Long taskId : values) {
			if (tts.getById().containsKey(taskId)) {
				Task task = tts.getById().get(taskId);
				if (violatesExpDateCondition(expDate, task)) {
					continue;
				}
				for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
					retval.add(new Object[] { taskId, entity.getId()});
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}
	
	private boolean violatesExpDateCondition(Date expDate, Task task) {
		if (optional) {
			return expDate != null 
					&& task.getTaskData().getExpirationTime() != null 
					&& expDate.equals(task.getTaskData().getExpirationTime());
		} else {
			return expDate != null 
					&& (task.getTaskData().getExpirationTime() == null 
					|| expDate.equals(task.getTaskData().getExpirationTime()));
		}
	}
	
    private void cleanTasksWithActualOwners(Set<Long> ids, Map<Long, Task> tasks) {
        List<Long> toRemove = new LinkedList<>();
        for (Long id : ids) {
                Task t = tasks.get(id);
                if (t.getTaskData().getActualOwner() != null) {
                        toRemove.add(id);
                }
        }
        ids.removeAll(toRemove);
    }
}
