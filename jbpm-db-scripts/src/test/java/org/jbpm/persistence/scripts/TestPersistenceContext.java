/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.persistence.scripts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jbpm.persistence.scripts.oldentities.ProcessInstanceInfo;
import org.jbpm.persistence.scripts.oldentities.SessionInfo;
import org.jbpm.persistence.scripts.oldentities.TaskImpl;
import org.jbpm.test.persistence.scripts.PersistenceUnit;
import org.jbpm.test.persistence.scripts.TestPersistenceContextBase;
import org.jbpm.test.persistence.scripts.util.TestsUtil;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalI18NText;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTaskData;

/**
 * Particular implementation of {@link TestPersistenceContextBase} class in order to add functionality related
 * to jBPM Human Task persistence.
 *
 * @see {@link TestPersistenceContextBase}
 */
public final class TestPersistenceContext extends TestPersistenceContextBase {

    /**
     * Creates and persists a simple mock human task
     */
    public void createSomeTask() {
        testIsInitialized();
        TaskImpl task = new TaskImpl();
        InternalI18NText name = (InternalI18NText) TaskModelProvider.getFactory().newI18NText();
        name.setText("Some Task");
        List<I18NText> names = new ArrayList<I18NText>();
        names.add(name);
        task.setNames(names);
        InternalTaskData taskData = (InternalTaskData) TaskModelProvider.getFactory().newTaskData();        
        taskData.setWorkItemId(12);
        taskData.setProcessInstanceId(1);
        taskData.setProcessId("someprocess");
        taskData.setDeploymentId("org.jbpm.test:someprocess:1.0");
        taskData.setProcessSessionId(1);
        task.setTaskData(taskData);
        InternalPeopleAssignments peopleAssignments = 
            (InternalPeopleAssignments) TaskModelProvider.getFactory().newPeopleAssignments();
        peopleAssignments.setPotentialOwners(new ArrayList<OrganizationalEntity>());
        peopleAssignments.setBusinessAdministrators(new ArrayList<OrganizationalEntity>());
        peopleAssignments.setExcludedOwners(new ArrayList<OrganizationalEntity>());
        peopleAssignments.setRecipients(new ArrayList<OrganizationalEntity>());
        peopleAssignments.setTaskStakeholders(new ArrayList<OrganizationalEntity>());
        InternalOrganizationalEntity jdoe = 
            (InternalOrganizationalEntity) TaskModelProvider.getFactory().newUser();
        jdoe.setId("jdoe");
        peopleAssignments.getPotentialOwners().add(jdoe);
        peopleAssignments.getBusinessAdministrators().add(jdoe);
        task.setPeopleAssignments(peopleAssignments);
        final boolean txOwner = transactionManager.begin();
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            em.persist(jdoe);
            em.persist(task);
            transactionManager.commit(txOwner);
        } catch (Exception ex) {
            ex.printStackTrace();
            transactionManager.rollback(txOwner);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static TestPersistenceContext createAndInitContext(PersistenceUnit persistenceUnit) {
        TestPersistenceContext testPersistenceContext = new TestPersistenceContext();
        testPersistenceContext.init(persistenceUnit);
        return testPersistenceContext;
    }

    /**
     * Persists a process and a session using entites from jBPM 6.0. Persists
     * process and session using their database identifiers so be aware that you can
     * rewrite some of your data. This method should be used only to populate inital data for tests.
     *
     * @param sessionId         Unique identifier of the session.
     * @param processId         Identifier of the process (name).
     * @param processInstanceId Unique identifier of the process.
     */
    public void persistOldProcessAndSession(final Integer sessionId, final String processId,
                                            final Long processInstanceId) {
        testIsInitialized();
        final boolean txOwner = transactionManager.begin();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.merge(getOldProcessInstanceInfo(processId, processInstanceId));
            entityManager.merge(getOldSessionInfo(sessionId));
            entityManager.flush();
            entityManager.close();
            transactionManager.commit(txOwner);
        } catch (Exception ex) {
            ex.printStackTrace();
            transactionManager.rollback(txOwner);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Returns ProcessInstanceInfo entity for jBPM 6.0 version.
     *
     * @param processId         Process identifier (name).
     * @param processInstanceId Unique identifier of the process. Database identifier.
     * @return ProcessInstanceInfo entity for jBPM 6.0 version filled with default data.
     * @throws ParseException
     */
    private ProcessInstanceInfo getOldProcessInstanceInfo(final String processId, final Long processInstanceId)
            throws ParseException {
        final DateFormat dateFormat = getDateFormat();
        final ProcessInstanceInfo result = new ProcessInstanceInfo();
        result.setProcessInstanceId(processInstanceId);
        result.setEventTypes(getOldProcessEventTypes());
        result.setLastModificationDate(dateFormat.parse("2015-08-25 13:43:25.760"));
        result.setLastReadDate(dateFormat.parse("2015-08-25 13:43:25.210"));
        result.setProcessId(processId);
        result.setStartDate(dateFormat.parse("2015-08-25 13:43:25.190"));
        result.setState(1);
        result.setVersion(2);
        result.setProcessInstanceByteArray(
                TestsUtil.hexStringToByteArray(
                        "ACED00057769000852756C65466C6F770A0608061004180052550A0852756C65466C6F7710011A0E6D696E696D616C50726F63657373200128023A0A0801100222020805280160006A0E5F6A62706D2D756E697175652D3072120A0E5F6A62706D2D756E697175652D311001800101"));
        return result;
    }

    /**
     * Returns SessionInfo entity for jBPM 6.0 version.
     *
     * @param sessionId Unique identifier of the session. Database identifier.
     * @return SessionInfo entity for jBPM 6.0 version filled with default data.
     * @throws ParseException
     */
    private SessionInfo getOldSessionInfo(final Integer sessionId) throws ParseException {
        final DateFormat dateFormat = getDateFormat();
        final SessionInfo result = new SessionInfo();
        result.setId(sessionId);
        result.setLastModificationDate(dateFormat.parse("2015-08-25 13:43:25.248"));
        result.setStartDate(dateFormat.parse("2015-08-25 13:43:24.858"));
        result.setVersion(2);
        result.setData(
                TestsUtil.hexStringToByteArray(
                        "ACED0005777C0A060806100418005272080010001A6818002000320608011000180042231A190A044D41494E10001801200028FFFFFFFFFFFFFFFFFF01400022060A044D41494E52350A0744454641554C54222A0A266F72672E64726F6F6C732E636F72652E726574656F6F2E496E697469616C46616374496D706C100022026800"));
        return result;
    }

    /**
     * Return default process event types.
     *
     * @return Default process event types.
     */
    private Set<String> getOldProcessEventTypes() {
        final Set<String> resultSet = new HashSet<String>(1);
        resultSet.add("test");
        return resultSet;
    }

    private DateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
}
