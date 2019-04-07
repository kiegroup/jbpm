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

package org.jbpm.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jbpm.services.task.HumanTaskConfigurator;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.util.SetupExamplesDatasource;
import org.kie.api.io.ResourceType;
import org.kie.api.KieBase;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMain {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMain.class);
    
	public static final void main(String[] args) throws Exception {
	    startUp();
		// load up the knowledge base
		KieBase kbase = readKnowledgeBase();
		StatefulKnowledgeSession ksession = newStatefulKnowledgeSession(kbase);
		// start a new process instance
		ksession.startProcess("com.sample.bpmn.hello");
		logger.info("Process started ...");
	}

	private static KieBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("humantask.bpmn"), ResourceType.BPMN2);
		return kbuilder.newKieBase();
	}
	
	private static void startUp() {
		SetupExamplesDatasource.setupPoolingDataSource();
		// please comment this line if you already have the task service running,
		// for example when running the jbpm-installer
		startTaskService();
	}

    public static TaskService startTaskService() {
        Properties properties = new Properties();
        String dialect = properties.getProperty("persistence.persistenceunit.dialect", "org.hibernate.dialect.H2Dialect");
        Map<String, String> map = new HashMap<String, String>();
        map.put("hibernate.dialect", dialect);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(properties.getProperty("taskservice.datasource.name", "org.jbpm.services.task"), map);
        System.setProperty("jbpm.user.group.mapping", properties.getProperty("taskservice.usergroupmapping", "classpath:/usergroups.properties"));
        TaskService taskService = new HumanTaskConfigurator()
                                        .entityManagerFactory(emf)
                                        .userGroupCallback(getUserGroupCallback())
                                        .getTaskService();
        return taskService;
    }	

    public static StatefulKnowledgeSession newStatefulKnowledgeSession(KieBase kbase) {
        return loadStatefulKnowledgeSession(kbase, -1);
    }


    public static StatefulKnowledgeSession loadStatefulKnowledgeSession(KieBase kbase, int sessionId) {
        Properties properties = new Properties();
        String persistenceEnabled = properties.getProperty("persistence.enabled", "false");
        RuntimeEnvironmentBuilder builder = null;
        if ("true".equals(persistenceEnabled)) {
            String dialect = properties.getProperty("persistence.persistenceunit.dialect", "org.hibernate.dialect.H2Dialect");
            Map<String, String> map = new HashMap<String, String>();
            map.put("hibernate.dialect", dialect);
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(properties.getProperty("persistence.persistenceunit.name", "org.jbpm.persistence.jpa"), map);
            builder = RuntimeEnvironmentBuilder.Factory.get()
        		.newDefaultBuilder()
                .entityManagerFactory(emf)                
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, com.arjuna.ats.jta.TransactionManager.transactionManager());
        } else {            
            builder = RuntimeEnvironmentBuilder.Factory.get()
        		.newDefaultInMemoryBuilder();
        }
        builder.knowledgeBase(kbase);
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(builder.get());
        return (StatefulKnowledgeSession) manager.getRuntimeEngine(EmptyContext.get()).getKieSession();
    }    

    @SuppressWarnings("unchecked")
    public static UserGroupCallback getUserGroupCallback() {
        Properties properties = new Properties();
        String className = properties.getProperty("taskservice.usergroupcallback");
        if (className != null) {
            try {
                Class<UserGroupCallback> clazz = (Class<UserGroupCallback>) Class.forName(className);
                return clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create instance of UserGroupCallback " + className, e);
            }
        } else {
            return new JBossUserGroupCallbackImpl("classpath:/usergroups.properties");
        }
    }

}
