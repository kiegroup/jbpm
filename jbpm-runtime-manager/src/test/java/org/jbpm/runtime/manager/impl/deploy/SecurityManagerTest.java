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

package org.jbpm.runtime.manager.impl.deploy;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.runtime.manager.util.TestUtil;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.util.AbstractBaseTest;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.SecurityManager;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.task.api.UserGroupCallback;

public class SecurityManagerTest extends AbstractBaseTest {
	
    private PoolingDataSourceWrapper pds;
    private UserGroupCallback userGroupCallback;  
    private RuntimeManager manager;
    @Before
    public void setup() {
        TestUtil.cleanupSingletonSessionId();
        pds = TestUtil.setupPoolingDataSource();
        Properties properties= new Properties();
        properties.setProperty("mary", "HR");
        properties.setProperty("john", "HR");
        userGroupCallback = new JBossUserGroupCallbackImpl(properties);
    }
    
    @After
    public void teardown() {
        if (manager != null) {
            manager.close();
        }
        pds.close();
    }

    @Test
	public void testNoSecurityManager() {
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newEmptyBuilder()
    			.userGroupCallback(userGroupCallback)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-ScriptTask.bpmn2"), ResourceType.BPMN2)
                .get();
        
        manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);        
        assertNotNull(manager);
        
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        assertNotNull(runtime);
        
        manager.disposeRuntimeEngine(runtime);
	}
    
    @Test(expected=SecurityException.class)
  	public void testDenyAllSecurityManager() {
  		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
      			.newEmptyBuilder()
      			.userGroupCallback(userGroupCallback)
                  .addAsset(ResourceFactory.newClassPathResource("BPMN2-ScriptTask.bpmn2"), ResourceType.BPMN2)
                  .get();
          
          manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);        
          assertNotNull(manager);
          ((InternalRuntimeManager) manager).setSecurityManager(new SecurityManager() {
			
			@Override
			public void checkPermission() throws SecurityException {
				throw new SecurityException("Deny all on purpose");
			}
          });
          manager.getRuntimeEngine(EmptyContext.get()).getKieSession();        
  	}
    
    @Test(expected=SecurityException.class)
	public void testCustomSecurityManager() {
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
    			.newEmptyBuilder()
    			.userGroupCallback(userGroupCallback)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-ScriptTask.bpmn2"), ResourceType.BPMN2)
                .get();
        
        manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);        
        assertNotNull(manager);
        final User user = new User("john");
        ((InternalRuntimeManager) manager).setSecurityManager(new SecurityManager() {

			@Override
			public void checkPermission() throws SecurityException {
				if ("mary".equals(user.getName())) {
					throw new SecurityException("Mary is not allowed to use runtime manager");
				}
			}
        	
        });
        
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        assertNotNull(runtime);
        
        manager.disposeRuntimeEngine(runtime);
        
        user.setName("mary");
        manager.getRuntimeEngine(EmptyContext.get()).getKieSession();
	}
    
    @Test(expected=IllegalStateException.class)
    public void testNoSessionAfterDispose() {
        ProcessEventListener processListener = new DefaultProcessEventListener();
        AgendaEventListener agendaListener = new DefaultAgendaEventListener();
        RuleRuntimeEventListener workingMemoryListener = new DefaultRuleRuntimeEventListener();
        Map<String,Object> globals = new HashMap<>();
        final List<String> list = new ArrayList<String>();
        globals.put("list", list);
        
        RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .userGroupCallback(userGroupCallback)
                .addAsset(ResourceFactory.newClassPathResource("BPMN2-BusinessRuleTaskWithGlobal.drl"), ResourceType.DRL)
                .registerableItemsFactory(new TestRegisterableItemsFactory(processListener, 
                                                                           agendaListener, 
                                                                           workingMemoryListener,
                                                                           globals))
                .get();
        
        manager = RuntimeManagerFactory.Factory.get().newPerProcessInstanceRuntimeManager(environment);        
        assertNotNull(manager);
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        assertNotNull(runtime);
        
        manager.disposeRuntimeEngine(runtime);
        runtime.getKieSession();
    }
    
    private class User {
    	private String name;

    	User(String name) {
    		this.name = name;
    	}
    	
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
    }
    
    private class TestRegisterableItemsFactory extends SimpleRegisterableItemsFactory {
        private ProcessEventListener plistener;
        private AgendaEventListener alistener;
        private RuleRuntimeEventListener rlistener;
        private Map<String, Object> globals;
        
        public TestRegisterableItemsFactory(ProcessEventListener listener, 
                                            AgendaEventListener alistener,
                                            RuleRuntimeEventListener rlistener,
                                            Map<String, Object> globals) {
            this.plistener = listener;
            this.alistener = alistener;
            this.rlistener = rlistener;
            this.globals = globals;
        }
        
        @Override
        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime){
            Map<String, WorkItemHandler> handlers = super.getWorkItemHandlers(runtime);
            handlers.put("MyWIH", new WorkItemHandler() {
                @Override
                public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                }

                @Override
                public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
                }});
            
            return handlers;
        }
        
        @Override
        public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
            List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
            if (plistener != null) {
                listeners.addAll(Arrays.asList(plistener));
            }
            
            return listeners;
        }
        @Override
        public List<AgendaEventListener> getAgendaEventListeners(RuntimeEngine runtime) {
            List<AgendaEventListener> alisteners = super.getAgendaEventListeners(runtime);
            if (alistener != null) { 
                alisteners.addAll(Arrays.asList(alistener));
            }
            return alisteners;
        }
        
        @Override
        public List<RuleRuntimeEventListener> getRuleRuntimeEventListeners(RuntimeEngine runtime) {
            List<RuleRuntimeEventListener> rlisteners = super.getRuleRuntimeEventListeners(runtime);
            if (rlistener != null) { 
                rlisteners.addAll(Arrays.asList(rlistener));
            }
            return rlisteners;
        }
        
        @Override
        public Map<String, Object> getGlobals(RuntimeEngine runtime){
            Map<String, Object> rglobals = super.getGlobals(runtime);
            if (rglobals != null) {
                rglobals.putAll(globals);
            }
            return globals;
        }
    }
}
