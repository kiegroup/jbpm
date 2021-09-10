/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.persistence.api.integration.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.model.TaskInstanceView;
import org.junit.Test;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.InternalPeopleAssignments;

public class BaseEventCollectionTest {

    @Test
    public void testEqualsEvents() {
        EventCollection events = new BaseEventCollection();
        Task task1 = new MyTaskImpl(1L, "javi", 1000, "alguno", "variado", new MyTaskDataImpl(), new MyPeople(new MyUser("javi")), "mucho espanol");
        events.add(new TaskInstanceView(task1));
        Task task2 = new MyTaskImpl(1L, "javi", 1000, "alguno", "variado", new MyTaskDataImpl(), new MyPeople(new MyUser("javi")), "poco espanol");
        events.add(new TaskInstanceView(task2));
        assertEquals(task1, task2);
        assertEquals(1, events.getEvents().size());
        assertEquals("poco espanol", ((TaskInstanceView) events.getEvents().iterator().next()).getDescription());
    }
    
    @Test 
    public void testNewSerialization() throws IOException, ClassNotFoundException {
        EventCollection events = new BaseEventCollection();
        Task task1 = new MyTaskImpl(1L, "javi", 1000, "alguno", "variado", new MyTaskDataImpl(), new MyPeople(new MyUser("javi")), "mucho espanol");
        events.add(new TaskInstanceView(task1));
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteArray)) {
            objectStream.writeObject(events);
        }
        assertValidCollection(readObject (new ByteArrayInputStream(byteArray.toByteArray())));
    }

    @Test
    public void testOldVersionDeserialization() throws ClassNotFoundException, IOException {
        assertValidCollection(readObject(getClass().getClassLoader().getResourceAsStream("BaseEventCollection.ser")));
    }
    
    private void assertValidCollection (EventCollection collection) {
        Collection<InstanceView<?>> events = collection.getEvents();
        assertFalse(events.isEmpty());
        TaskInstanceView view = (TaskInstanceView) events.iterator().next();
        assertEquals(1L, view.getId().longValue());
        assertEquals("javi", view.getName());
        assertEquals(1000, view.getPriority().intValue());
        assertEquals("alguno", view.getSubject());
        assertEquals("variado", view.getFormName());
        assertEquals("mucho espanol", view.getDescription());
    }

    private static EventCollection readObject(InputStream input) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(input)) {
            return (EventCollection) stream.readObject();
        }
    }

    private static class MyTaskImpl implements Task {

        private Long id;
        private String name;
        private Integer priority;
        private String subject;
        private String formName;
        private TaskData taskData;
        private PeopleAssignments peopleAssigments;
        private String description;

        public MyTaskImpl(Long id, String name, Integer priority, String subject, String formName, TaskData taskData, PeopleAssignments peopleAssigments, String description) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.subject = subject;
            this.formName = formName;
            this.taskData = taskData;
            this.peopleAssigments = peopleAssigments;
            this.description = description;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(id);
            out.writeUTF(name);
            out.writeInt(priority);
            out.writeUTF(subject);
            out.writeUTF(formName);
            out.writeObject(taskData);
            out.writeObject(peopleAssigments);
            out.writeUTF(description);

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = in.readLong();
            name = in.readUTF();
            priority = in.readInt();
            subject = in.readUTF();
            formName = in.readUTF();
            taskData = (TaskData) in.readObject();
            peopleAssigments = (PeopleAssignments) in.readObject();
            description = in.readUTF();

        }

        @Override
        public String toString() {
            return "MyTaskImpl [id=" + id + ", name=" + name + ", priority=" + priority + ", subject=" + subject + ", formName=" + formName + ", taskData=" + taskData + ", peopleAssigments=" + peopleAssigments +
                   ", description=" + description + "]";
        }

        @Override
        public Long getId() {

            return id;
        }

        @Override
        public Integer getPriority() {

            return priority;
        }

        @Override
        public List<I18NText> getNames() {

            return Collections.emptyList();
        }

        @Override
        public List<I18NText> getSubjects() {

            return Collections.emptyList();
        }

        @Override
        public List<I18NText> getDescriptions() {

            return Collections.emptyList();
        }

        @Override
        public String getName() {

            return name;
        }

        @Override
        public String getSubject() {

            return subject;
        }

        @Override
        public String getDescription() {

            return description;
        }

        @Override
        public PeopleAssignments getPeopleAssignments() {

            return peopleAssigments;
        }

        @Override
        public TaskData getTaskData() {

            return taskData;
        }

        @Override
        public String getTaskType() {

            return "alwaysTheSame";
        }

        @Override
        public Boolean isArchived() {

            return false;
        }

        @Override
        public Integer getVersion() {

            return 1;
        }

        @Override
        public String getFormName() {

            return formName;
        }

        @Override
        public int hashCode() {
            return id.intValue();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof MyTaskImpl)) {
                return false;
            }
            MyTaskImpl other = (MyTaskImpl) obj;
            return id.longValue() == other.id.longValue();
        }

    }

    private static class MyTaskDataImpl implements TaskData {

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        }

        @Override
        public Status getStatus() {

            return Status.Completed;
        }

        @Override
        public Status getPreviousStatus() {

            return null;
        }

        @Override
        public User getActualOwner() {

            return null;
        }

        @Override
        public User getCreatedBy() {

            return null;
        }

        @Override
        public Date getCreatedOn() {

            return null;
        }

        @Override
        public Date getActivationTime() {

            return null;
        }

        @Override
        public Date getExpirationTime() {

            return null;
        }

        @Override
        public boolean isSkipable() {

            return false;
        }

        @Override
        public long getWorkItemId() {

            return 0;
        }

        @Override
        public long getProcessInstanceId() {

            return 0;
        }

        @Override
        public String getProcessId() {

            return null;
        }

        @Override
        public String getDeploymentId() {

            return null;
        }

        @Override
        public long getProcessSessionId() {

            return 0;
        }

        @Override
        public String getDocumentType() {

            return null;
        }

        @Override
        public long getDocumentContentId() {

            return 0;
        }

        @Override
        public String getOutputType() {

            return null;
        }

        @Override
        public Long getOutputContentId() {

            return null;
        }

        @Override
        public String getFaultName() {

            return null;
        }

        @Override
        public String getFaultType() {

            return null;
        }

        @Override
        public long getFaultContentId() {

            return 0;
        }

        @Override
        public List<Comment> getComments() {

            return null;
        }

        @Override
        public List<Attachment> getAttachments() {

            return null;
        }

        @Override
        public long getParentId() {

            return 0;
        }

        @Override
        public Map<String, Object> getTaskInputVariables() {

            return null;
        }

        @Override
        public Map<String, Object> getTaskOutputVariables() {

            return null;
        }

    }

    private static class MyPeople implements InternalPeopleAssignments {

        private User user;

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(user);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            user = (User) in.readObject();
        }

        @Override
        public User getTaskInitiator() {
            return user;
        }

        @Override
        public List<OrganizationalEntity> getPotentialOwners() {

            return Collections.emptyList();
        }

        @Override
        public List<OrganizationalEntity> getBusinessAdministrators() {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "MyPeople [user=" + user + "]";
        }

        public MyPeople(User user) {
            this.user = user;
        }

        @Override
        public void setTaskInitiator(User taskInitiator) {

        }

        @Override
        public void setPotentialOwners(List<OrganizationalEntity> potentialOwners) {

        }

        @Override
        public List<OrganizationalEntity> getExcludedOwners() {
            return Collections.emptyList();
        }

        @Override
        public void setExcludedOwners(List<OrganizationalEntity> excludedOwners) {

        }

        @Override
        public List<OrganizationalEntity> getTaskStakeholders() {
            return Collections.emptyList();
        }

        @Override
        public void setTaskStakeholders(List<OrganizationalEntity> taskStakeholders) {

        }

        @Override
        public void setBusinessAdministrators(List<OrganizationalEntity> businessAdministrators) {

        }

        @Override
        public List<OrganizationalEntity> getRecipients() {
            return Collections.emptyList();
        }

        @Override
        public void setRecipients(List<OrganizationalEntity> recipients) {

        }

    }

    private static class MyUser implements User {

        private String id;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {

            out.writeUTF(id);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            id = in.readUTF();

        }

        public MyUser(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "MyUser [id=" + id + "]";
        }

    }
}
