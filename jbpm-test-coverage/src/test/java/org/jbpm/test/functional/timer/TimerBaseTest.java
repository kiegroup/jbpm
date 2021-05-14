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

package org.jbpm.test.functional.timer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.test.AbstractBaseTest;
import org.jbpm.test.persistence.scripts.util.ScriptFilter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.test.util.db.PoolingDataSourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jbpm.test.persistence.scripts.ScriptsBase.executeScriptRunner;
import static org.jbpm.test.persistence.scripts.util.ScriptFilter.filter;
import static org.jbpm.test.persistence.scripts.util.TestsUtil.getDatabaseType;

public abstract class TimerBaseTest extends AbstractBaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TimerBaseTest.class);
	
	private static PoolingDataSourceWrapper pds;
    protected ScriptFilter createScript = filter("quartz_tables_" + getDatabaseType().getScriptDatabasePrefix() + ".sql");
    protected ScriptFilter dropScript = filter("quartz_tables_drop_" + getDatabaseType().getScriptDatabasePrefix() + ".sql");
    protected static final String DB_DDL_SCRIPTS_RESOURCE_PATH = "/db/ddl-scripts";

    @BeforeClass
    public static void setUpOnce() {
        if (pds == null) {
            pds = setupPoolingDataSource(DATASOURCE_NAME);
        }
    }

    @AfterClass
    public static void tearDownOnce() {
        if (pds != null) {
            pds.close();
            pds = null;
        }
    }

    protected void createTimerSchema() throws IOException, SQLException {
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, createScript, pds, getDataSourceProperties().getProperty("defaultSchema"));
    }

    protected void dropTimerSchema() throws IOException, SQLException {
        executeScriptRunner(DB_DDL_SCRIPTS_RESOURCE_PATH, dropScript, pds, getDataSourceProperties().getProperty("defaultSchema"));
    }
    
    protected class TestRegisterableItemsFactory extends DefaultRegisterableItemsFactory {
        private ProcessEventListener[] plistener;
        private AgendaEventListener[] alistener;
        private TaskLifeCycleEventListener[] tlistener;
        
        public TestRegisterableItemsFactory(ProcessEventListener... listener) {
            this.plistener = listener;
        }
        
        public TestRegisterableItemsFactory(AgendaEventListener... listener) {
            this.alistener = listener;
        }
        
        public TestRegisterableItemsFactory(TaskLifeCycleEventListener... tlistener) {
            this.tlistener = tlistener;
        }

        @Override
        public List<ProcessEventListener> getProcessEventListeners(
                RuntimeEngine runtime) {
            
            List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
            if (plistener != null) {
                listeners.addAll(Arrays.asList(plistener));
            }
            
            return listeners;
        }
        @Override
        public List<AgendaEventListener> getAgendaEventListeners(
                RuntimeEngine runtime) {
            
            List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
            if (alistener != null) { 
                listeners.addAll(Arrays.asList(alistener));
            }
            
            return listeners;
        }

        @Override
        public List<TaskLifeCycleEventListener> getTaskListeners() {

            List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
            if (tlistener != null) {
                listeners.addAll(Arrays.asList(tlistener));
            }
            return listeners;
        } 
        
    }
}
