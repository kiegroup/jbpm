package org.jbpm.tasks.admin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.jbpm.task.AccessType;
import org.jbpm.task.Content;
import org.jbpm.task.I18NText;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.jbpm.task.service.ContentData;
import org.junit.Assert;

/**
 * Provides methods for:
 * <ul>
 * <li>populating the DB with fake task data,</li>
 * <li>verification that this data has not been changed during the test,</li>
 * <li>removing fake task data from DB.</li>
 * </ul>
 */
public class TaskTestEnvironment {

    private final EntityManagerFactory emf;
    
    private Task createdTask;

    private Content createdContent;

    private User createdUser;
    
    public TaskTestEnvironment(final EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Inserts test task data into the database.
     */
    public void insertFakeTaskData() {
        final EntityManager em = emf.createEntityManager();
        final EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            final Task task = FakeTaskFactory.createTask();
            em.persist(task);
            
            final ContentData contentData = FakeTaskFactory.createContentData();
            final Content content = new Content(contentData.getContent());
            em.persist(content);
            task.getTaskData().setDocument(content.getId(), contentData);

            final User taskUser = FakeTaskFactory.createUser();
            em.persist(taskUser);
            task.setPeopleAssignments(FakeTaskFactory.createPeopleAssignments(taskUser));
            
            this.createdTask = task;
            this.createdContent = content;
            this.createdUser = taskUser;
            
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("Unable to insert test task data.", e);
        } finally {
            em.close();
        }
    }

    /**
     * Verifies that the fake task data has not been changed in the database. It
     * must not be called before {@link #insertTaskData() }.
     */
    public void verifyFakeTaskDataNotChanged() {
        final EntityManager em = emf.createEntityManager();
        try {
            final Task foundTask = em.find(Task.class, createdTask.getId());
            em.refresh(foundTask);

            Assert.assertEquals("unexpected task id", 
                    createdTask.getId(), foundTask.getId());
            Assert.assertArrayEquals("unexpected task names", 
                    createdTask.getNames().toArray(),
                    foundTask.getNames().toArray());
            Assert.assertEquals("unexpected task doc content id", 
                    createdTask.getTaskData().getDocumentContentId(),
                    foundTask.getTaskData().getDocumentContentId());

            Assert.assertEquals("unexpected people assignments",
                    createdTask.getPeopleAssignments(),
                    foundTask.getPeopleAssignments());
            
            final Content foundContent = em.find(Content.class, createdContent.getId());
            em.refresh(foundContent);

            Assert.assertEquals("unexpected content id", 
                    createdContent.getId(), foundContent.getId());
            Assert.assertArrayEquals("unexpected content data", 
                    createdContent.getContent(), foundContent.getContent());
            
            // TODO other assertions
        } finally {
            em.close();
        }
    }

    /**
     * Verifies that the database contains only task data inserted
     * by {@link #insertTaskData() }.
     */
    public void verifyFakeTaskDataAloneInDb() {
        final EntityManager em = emf.createEntityManager();
        try {
            List results;
            results = em.createQuery("select t from Task t").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected Task in DB",
                    createdTask.getId(), ((Task) results.get(0)).getId());

            results = em.createQuery("select i from I18NText i").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected I18NText in DB",
                    createdTask.getNames().get(0), ((I18NText) results.get(0)));

            results = em.createQuery("select c from Content c").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected Content in DB",
                    createdContent.getId(), ((Content) results.get(0)).getId());

            // verify people assignments assoc. tables
            results = em.createNativeQuery("select task_id from PeopleAssignments_BAs").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected PeopleAssignments_BAs in DB",
                    BigInteger.valueOf(createdTask.getId()), results.get(0));

            results = em.createNativeQuery("select task_id from PeopleAssignments_ExclOwners").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected PeopleAssignments_ExclOwners in DB",
                    BigInteger.valueOf(createdTask.getId()), results.get(0));

            results = em.createNativeQuery("select task_id from PeopleAssignments_PotOwners").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected PeopleAssignments_PotOwners in DB",
                    BigInteger.valueOf(createdTask.getId()), results.get(0));

            results = em.createNativeQuery("select task_id from PeopleAssignments_Recipients").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected PeopleAssignments_Recipients in DB",
                    BigInteger.valueOf(createdTask.getId()), results.get(0));

            results = em.createNativeQuery("select task_id from PeopleAssignments_Stakeholders").getResultList();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("unexpected PeopleAssignments_Stakeholders in DB",
                    BigInteger.valueOf(createdTask.getId()), results.get(0));
            
        } finally {
            em.close();
        }
    }

    /**
     * Removes previously inserted test task data from the database. It must not
     * be called before {@link #insertTaskData() }.
     */
    public void removeFakeTaskData() {
        if (createdTask != null) {
            final EntityManager em = emf.createEntityManager();
            final EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                final Task task = em.find(Task.class, createdTask.getId());
                em.remove(task);
                
                final Content content = em.find(Content.class, createdContent.getId());
                em.remove(content);

                final User user = em.find(User.class, createdUser.getId());
                em.remove(user);
                
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw new RuntimeException("Unable to remove test task data.", e);
            } finally {
                em.close();
            }
        }
    }

    public static class FakeTaskFactory {

        public static final long DOCUMENT_ID = 1000;
        
        public static final int PRIORITY = 10;
        
        public static final byte[] CONTENT_DATA = new byte[] { 1, 2, 3 };
       
        public static Task createTask() {
            final Task task = new Task();
            final TaskData taskData = new TaskData();
            task.setPriority(PRIORITY);
            task.setNames(createTaskNameList());
            task.setTaskData(taskData);
            return task;
        }

        public static User createUser() {
            return new User("FakeTaskUser");
        }
        
        public static ContentData createContentData() {
            final ContentData contentData = new ContentData();
            contentData.setAccessType(AccessType.Inline);
            contentData.setContent(CONTENT_DATA);
            return contentData;
        }
        
        public static PeopleAssignments createPeopleAssignments(
                final OrganizationalEntity assignedEntity) {
            
            final PeopleAssignments assignments = new PeopleAssignments();
            assignments.setBusinessAdministrators(wrapOrgEntity(assignedEntity));
            assignments.setExcludedOwners(wrapOrgEntity(assignedEntity));
            assignments.setPotentialOwners(wrapOrgEntity(assignedEntity));
            assignments.setRecipients(wrapOrgEntity(assignedEntity));
            assignments.setTaskStakeholders(wrapOrgEntity(assignedEntity));
            return assignments;
        }

        private static List<I18NText> createTaskNameList() {
            final List<I18NText> nameList = new ArrayList<I18NText>(1);
            nameList.add(new I18NText("en",
                    FakeTaskFactory.class.getSimpleName() + " task"));
            return nameList;
        }

        private static List<OrganizationalEntity> wrapOrgEntity(final OrganizationalEntity entity) {
            final List<OrganizationalEntity> bas = new ArrayList<OrganizationalEntity>(1);
            bas.add(entity);
            return bas;
        }
    }
}
