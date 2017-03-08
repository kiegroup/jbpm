/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.services.task.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.services.task.impl.model.AttachmentImpl;
import org.jbpm.services.task.impl.model.CommentImpl;
import org.jbpm.services.task.impl.model.ContentImpl;
import org.jbpm.services.task.impl.model.DeadlineImpl;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.persistence.query.MapDBQuery;
import org.jbpm.services.task.persistence.query.MapDBQueryRegistry;
import org.kie.api.persistence.ObjectStoringStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.FaultData;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;
import org.mapdb.Atomic;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDBTaskPersistenceContext implements TaskPersistenceContext {

    // logger set to public for test reasons, see the org.jbpm.services.task.TaskQueryBuilderLocalTest
	public final static Logger logger = LoggerFactory.getLogger(MapDBTaskPersistenceContext.class);

	protected DB db;
	protected TaskTableService tts;
	private TransactionManager txm;
	private Map<Long, Task> taskCache = new HashMap<>();

	private HTreeMap<Long, Task> taskById;
	private BTreeMap<Long, Content> contents;
	private BTreeMap<Long, Comment> comments;
	private BTreeMap<Long, Attachment> attachments;
	private BTreeMap<Long, Deadline> deadlines;
	private BTreeMap<String, OrganizationalEntity> orgEntities;

	private Atomic.Long nextId;
	private Atomic.Long nextDeadlineId;

	private UserGroupCallback callback;

	private Environment env;


    // Interface methods ----------------------------------------------------------------------------------------------------------

	public MapDBTaskPersistenceContext(Environment environment) {
		this.env = environment;
		this.db = (DB) environment.get(MapDBEnvironmentName.DB_OBJECT);
		ObjectStoringStrategy[] strategies = (ObjectStoringStrategy[]) environment.get(MapDBEnvironmentName.OBJECT_STORING_STRATEGIES);
		this.tts = new TaskTableService(db, strategies);
		this.txm = (TransactionManager) environment.get(EnvironmentName.TRANSACTION_MANAGER);
		this.callback = (UserGroupCallback) environment.get(EnvironmentName.TASK_USER_GROUP_CALLBACK);
		this.taskById = db.hashMap("taskById", 
				new SerializerLong(), 
				new TaskSerializer()).createOrOpen();
		this.orgEntities = db.treeMap("orgEntity", 
				Serializer.STRING, 
				new OrganizationalEntitySerializer()).createOrOpen();
		this.contents = db.treeMap("contents",
				Serializer.LONG,
				new TaskContentSerializer()).createOrOpen();
		this.comments = db.treeMap("comments",
				Serializer.LONG,
				new TaskCommentSerializer()).createOrOpen();
		this.attachments = db.treeMap("attachments",
				Serializer.LONG,
				new TaskAttachmentSerializer()).createOrOpen();
		this.deadlines = db.treeMap("deadlines",
				Serializer.LONG,
				new TaskDeadlineSerializer()).createOrOpen();
		nextId = db.atomicLong("taskId").createOrOpen();
		nextDeadlineId = db.atomicLong("deadlineId").createOrOpen();
	}

	@Override
	public Task findTask(Long taskId) {
		if (taskId == null) {
			return null;
		}
		if (taskCache.containsKey(taskId)) {
			return taskCache.get(taskId);
		}
		if (!tts.getById().containsKey(taskId)) {
			return null;
		}
		Task task = tts.getById().get(taskId);
		if (task != null) {
			TaskTransactionHelper.addToUpdatableSet(txm, (MapDBElement) task);
			taskCache.put(taskId, task);
		}
		return task;
	}

	@Override
	public Task persistTask(Task task) {
		if (task != null) {
			if (task.getId() == null || task.getId() <= 0) {
				((TaskImpl) task).setId(nextId.incrementAndGet());
			}
			setDeadlinesId((TaskImpl) task);
			TaskTransactionHelper.addToUpdatableSet(txm, (MapDBElement) task);
		}
		tts.update(task);
		/*TaskKey key = new TaskKey(task);
		this.tasks.put(key, task);*/
        return task;
	}

	private void setDeadlinesId(TaskImpl task) {
		Deadlines dl = task.getDeadlines();
		if (dl != null) {
			List<Deadline> startDeadlines = dl.getStartDeadlines();
			if (startDeadlines != null) {
				for (Deadline d : startDeadlines) {
					if (d.getId() <= 0) {
						d.setId(nextDeadlineId.incrementAndGet());
					}
				}
			}
			List<Deadline> endDeadlines = dl.getEndDeadlines();
			if (endDeadlines != null) {
				for (Deadline d : endDeadlines) {
					if (d.getId() <= 0) {
						d.setId(nextDeadlineId.incrementAndGet());
					}
				}
			}
		}
		
	}

	@Override
	public Task updateTask(Task task) {
		return persistTask(task);
	}

	@Override
	public Task removeTask(Task task) {
		tts.remove(task.getId());
		if (task != null) {
			TaskTransactionHelper.removeFromUpdatableSet(txm, (MapDBElement) task);
		}
		return task;
	}

	@Override
	public Group findGroup(String groupId) {
		OrganizationalEntity value = orgEntities.getOrDefault(groupId, null);
		if (value instanceof Group) {
			return (Group) value;
		}
		return null;
	}

	@Override
	public Group persistGroup(Group group) {
		if (orgEntities.containsKey(group.getId())) {
			OrganizationalEntity entity = orgEntities.get(group.getId());
			if (!(entity instanceof Group)) {
				throw new RuntimeException("Group already exists with " + group
    				+ " id, please check that there is no group and user with same id");
			}
		}
		orgEntities.put(group.getId(), group);
		return group;
	}

	@Override
	public Group updateGroup(Group group) {
		return persistGroup(group);
	}

	@Override
	public Group removeGroup(Group group) {
		orgEntities.remove(group.getId());
		return group;
	}

	@Override
	public User findUser(String userId) {
		OrganizationalEntity value = orgEntities.getOrDefault(userId, null);
		if (value instanceof User) {
			return (User) value;
		}
		return null;
	}

	@Override
	public User persistUser(User user) {
		if (orgEntities.containsKey(user.getId())) {
			OrganizationalEntity entity = orgEntities.get(user.getId());
			if (!(entity instanceof User)) {
				throw new RuntimeException("User already exists with " + user
	    				+ " id, please check that there is no group and user with same id");
			}
		}
		orgEntities.put(user.getId(), user);
        return user;
	}

	@Override
	public User updateUser(User user) {
		return persistUser(user);
	}

	@Override
	public User removeUser(User user) {
		orgEntities.remove(user.getId());
		return user;
	}

	@Override
	public OrganizationalEntity findOrgEntity(String orgEntityId) {
		if (!orgEntities.containsKey(orgEntityId)) {
			return null;
		}
		return orgEntities.get(orgEntityId);
	}

	@Override
	public OrganizationalEntity persistOrgEntity(OrganizationalEntity orgEntity) {
		if (orgEntity instanceof User) {
			persistUser((User) orgEntity);
		} else if (orgEntity instanceof Group) {
			persistGroup((Group) orgEntity);
		}
        return orgEntity;
	}

	@Override
	public OrganizationalEntity updateOrgEntity(OrganizationalEntity orgEntity) {
		return persistOrgEntity(orgEntity);
	}

	@Override
	public OrganizationalEntity removeOrgEntity(OrganizationalEntity orgEntity) {
		if (orgEntity instanceof User) {
			removeUser((User) orgEntity);
		} else if (orgEntity instanceof Group) {
			removeGroup((Group) orgEntity);
		}
		return orgEntity;
	}

	@Override
	public Content findContent(Long contentId) {
		return this.contents.getOrDefault(contentId, null);
	}

	@Override
	public Content persistContent(Content content) {
		if (content.getId() == null || content.getId() <= 0) {
			((ContentImpl) content).setId(nextId.incrementAndGet());
		}
		this.contents.put(content.getId(), content);
		return content;
	}

	@Override
	public Content updateContent(Content content) {
		return persistContent(content);
	}

	@Override
	public Content removeContent(Content content) {
		this.contents.remove(content.getId());
		return content;
	}

	@Override
	public Task setDocumentToTask(Content content, ContentData contentData,
			Task task) {
		Long id = 0L;
		if (content != null) {
			id = content.getId();
		}
		((InternalTaskData) task.getTaskData()).setDocument(id, contentData);
		tts.storeContent(task.getId(), id, contentData.getContentObject());
		TaskTransactionHelper.addToUpdatableSet(txm, (MapDBElement) task);
		return task;
	}
	
	@Override
	public Task setFaultToTask(Content content, FaultData faultData, Task task) {
		Long id = 0L;
		if (content != null) {
			id = content.getId();
		}
		((InternalTaskData) task.getTaskData()).setFault(id, faultData);
		tts.storeContent(task.getId(), id, faultData.getContentObject());
		TaskTransactionHelper.addToUpdatableSet(txm, (MapDBElement) task);
		return task;
	}
	
	@Override
	public Task setOutputToTask(Content content, ContentData contentData,
			Task task) {
		Long id = 0L;
		if (content != null) {
			id = content.getId();
		}
		((InternalTaskData) task.getTaskData()).setOutput(id, contentData);
		tts.storeContent(task.getId(), id, contentData.getContentObject());
		TaskTransactionHelper.addToUpdatableSet(txm, (MapDBElement) task);
		return task;	
	}
	
	@Override
	public Attachment findAttachment(Long attachmentId) {
		return this.attachments.getOrDefault(attachmentId, null);
	}

	@Override
	public Attachment persistAttachment(Attachment attachment) {
		if (attachment.getId() == null || attachment.getId() <= 0) {
			((AttachmentImpl) attachment).setId(nextId.incrementAndGet());
		}
		this.attachments.put(attachment.getId(), attachment);
		return attachment;
	}

	@Override
	public Attachment updateAttachment(Attachment attachment) {
		return persistAttachment(attachment);
	}

	@Override
	public Attachment removeAttachment(Attachment attachment) {
		this.attachments.remove(attachment.getId());
		return attachment;
	}
	
	@Override
	public Attachment addAttachmentToTask(Attachment attachment, Task task) {
		((InternalTaskData) task.getTaskData()).addAttachment(attachment);
		tts.addTaskContentRelation(task, attachment.getAttachmentContentId());
		return attachment;
	}
	
	@Override
	public Attachment removeAttachmentFromTask(Task task, long attachmentId) {
		Attachment attachment = ((InternalTaskData) task.getTaskData()).removeAttachment(attachmentId);
		tts.removeTaskContentRelation(task, attachment.getAttachmentContentId());
		return attachment;
	}

	@Override
	public Comment findComment(Long commentId) {
		return this.comments.getOrDefault(commentId, null);
	}

	@Override
	public Comment persistComment(Comment comment) {
		if (comment.getId() == null || comment.getId() <= 0) {
			((CommentImpl) comment).setId(nextId.incrementAndGet());
		}
		this.comments.put(comment.getId(), comment);
		return comment;
	}

	@Override
	public Comment updateComment(Comment comment) {
		return persistComment(comment);
	}

	@Override
	public Comment removeComment(Comment comment) {
		this.comments.remove(comment.getId());
		return comment;
	}
	
	@Override
	public Comment addCommentToTask(Comment comment, Task task) {
		((InternalTaskData) task.getTaskData()).addComment(comment);
		return comment;
	}
	
	@Override
	public Comment removeCommentFromTask(Comment comment, Task task) {
		((InternalTaskData) task.getTaskData()).removeComment(comment.getId());
		return comment;
	}

	@Override
	public Deadline findDeadline(Long deadlineId) {
		long[] taskIds = tts.getByDeadlineId().get(deadlineId);
		if (taskIds != null) {
			for (long taskId : taskIds) {
				InternalTask task = (InternalTask) tts.getById().get(taskId);
				if (task.getDeadlines() != null) {
					if (task.getDeadlines().getStartDeadlines() != null) {
						for (Deadline d : task.getDeadlines().getStartDeadlines()) {
							if (deadlineId.equals(d.getId())) {
								return d;
							}
						}
					}
					if (task.getDeadlines().getEndDeadlines() != null) {
						for (Deadline d : task.getDeadlines().getEndDeadlines()) {
							if (deadlineId.equals(d.getId())) {
								return d;
							}
						}
					}
				}
			}
		}
		return this.deadlines.getOrDefault(deadlineId, null);
	}

	@Override
	public Deadline persistDeadline(Deadline deadline) {
		if (deadline.getId() <= 0) {
			((DeadlineImpl) deadline).setId(nextId.incrementAndGet());
		}
		this.deadlines.put(deadline.getId(), deadline);
		return deadline;
	}

	@Override
	public Deadline updateDeadline(Deadline deadline) {
		return persistDeadline(deadline);
	}

	@Override
	public Deadline removeDeadline(Deadline deadline) {
		this.deadlines.remove(deadline.getId());
		return deadline;
	}

	@Override @SuppressWarnings("unchecked")
	public <T> T queryWithParametersInTransaction(String queryName,
			Map<String, Object> params, Class<T> clazz) {
		System.out.println("queryWithParametersInTransaction: " + queryName);
		MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery(queryName);
		if (query == null) {
			throw new UnsupportedOperationException("Not implemented yet: " + queryName);
		}
		return (T) query.execute(callback, params, tts, false);
	}

    @Override  @SuppressWarnings("unchecked")
	public <T> T queryWithParametersInTransaction(String queryName, boolean singleResult,
			Map<String, Object> params, Class<T> clazz) {
    	System.out.println("queryWithParametersInTransaction: " + queryName);
    	MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery(queryName);
    	if (query == null) {
    		throw new UnsupportedOperationException("Not implemented yet: " + queryName);
    	}
    	return (T) query.execute(callback, params, tts, singleResult);
	}

	@Override
	public <T> T queryAndLockWithParametersInTransaction(String queryName,
			Map<String, Object> params, boolean singleResult, Class<T> clazz) {
		return queryWithParametersInTransaction(queryName, singleResult, params, clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T queryInTransaction(String queryName, Class<T> clazz) {
		MapDBQuery<?> query = MapDBQueryRegistry.getInstance().getQuery(queryName);
		if (query == null) {
			throw new UnsupportedOperationException("Query " + queryName + " not implemented");
		}
		return (T) query.execute(callback, new HashMap<>(), tts, false);
	}

	@Override
	public <T> T queryStringInTransaction(String queryString, Class<T> clazz) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T queryStringWithParametersInTransaction(String queryString,
			Map<String, Object> params, Class<T> clazz) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T queryStringWithParametersInTransaction(String queryString, boolean singleResult,
			Map<String, Object> params, Class<T> clazz) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T queryAndLockStringWithParametersInTransaction(
			String queryName, Map<String, Object> params, boolean singleResult,
			Class<T> clazz) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public int executeUpdateString(String updateString) {
		throw new UnsupportedOperationException("Not supported by MapDB");
	}
	
	@Override
	public int executeUpdate(String queryName, Map<String, Object> params) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override //TODO make this method part of an abstract implementation
	public HashMap<String, Object> addParametersToMap(Object... parameterValues) {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
        if( parameterValues.length % 2 != 0 ) {
            throw new RuntimeException("Expected an even number of parameters, not " + parameterValues.length);
        }
        for( int i = 0; i < parameterValues.length; ++i ) {
            String parameterName = null;
            if( parameterValues[i] instanceof String ) {
                parameterName = (String) parameterValues[i];
            } else {
                throw new RuntimeException("Expected a String as the parameter name, not a " + parameterValues[i].getClass().getSimpleName());
            }
            ++i;
            parameters.put(parameterName, parameterValues[i]);
        }
        return parameters;
	}

	@Override
	public <T> T persist(T object) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T remove(T entity) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public <T> T merge(T entity) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
	}

	@Override
	public boolean isOpen() {
		return !db.isClosed();
	}

	@Override
	public void joinTransaction() {
	}

	@Override
	public void close() {
		taskCache.clear();
	}

    @Override
    public Long findTaskIdByContentId( Long contentId ) {
    	long[] values = tts.getByContentId().get(contentId);
    	if (values != null) {
    		Task t = taskById.get(values[0]);
    		if (t != null) {
    			return t.getId();
    		}
    	}
    	return null;
    }

    @Override
    public List<TaskSummary> doTaskSummaryCriteriaQuery(String userId, UserGroupCallback userGroupCallback, Object queryWhere) {
		throw new UnsupportedOperationException("Not implemented yet");//TODO
    }

	public DB getDB() {
		return db;
	}

}

