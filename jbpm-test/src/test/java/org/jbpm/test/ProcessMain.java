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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorManager;
import org.kie.test.util.db.PersistenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMain {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMain.class);
    private static final boolean usePersistence = true;
    
	public static final void main(String[] args) throws Exception {
		cleanupSingletonSessionId();
		// load up the knowledge base
		KieBase kbase = readKnowledgeBase();
		StatefulKnowledgeSession ksession = newStatefulKnowledgeSession(kbase);
		// start a new process instance
		ksession.startProcess("com.sample.bpmn.hello");
		logger.info("Process started ...");
		System.exit(0);
	}

	private static KieBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("simple.bpmn"), ResourceType.BPMN2);
		return kbuilder.newKieBase();
	}
	
    public static StatefulKnowledgeSession newStatefulKnowledgeSession(KieBase kbase) {
    	RuntimeEnvironmentBuilder builder = null;
    	if ( usePersistence ) {
			Properties properties = new Properties();
			properties.put("driverClassName", "org.h2.Driver");
			properties.put("className", "org.h2.jdbcx.JdbcDataSource");
			properties.put("user", "sa");
			properties.put("password", "");
			properties.put("url", "jdbc:h2:tcp://localhost/~/jbpm-db");
			properties.put("datasourceName", "jdbc/jbpm-ds");
			PersistenceUtil.setupPoolingDataSource(properties);
		    Map<String, String> map = new HashMap<String, String>();
		    map.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
	        EntityManagerFactory emf = Persistence.createEntityManagerFactory(properties.getProperty("persistence.persistenceunit.name", "org.jbpm.persistence.jpa"), map);            	            
	        builder = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultBuilder()
                .entityManagerFactory(emf);
    	} else {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                .newDefaultInMemoryBuilder();
            DeploymentDescriptor descriptor = 
				new DeploymentDescriptorManager().getDefaultDescriptor().getBuilder().auditMode(AuditMode.NONE).get();	
            builder.addEnvironmentEntry("KieDeploymentDescriptor", descriptor);                
        }
        builder.knowledgeBase(kbase);
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(builder.get());
        return (StatefulKnowledgeSession) manager.getRuntimeEngine(EmptyContext.get()).getKieSession();
    }

    private static void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {
            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {                    
                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                new File(tempDir, file).delete();
            }
        }
    }}
