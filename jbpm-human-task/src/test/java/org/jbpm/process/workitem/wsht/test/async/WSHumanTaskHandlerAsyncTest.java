/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.workitem.wsht.test.async;

import static org.jbpm.task.service.test.impl.TestServerUtil.*;

import org.jbpm.process.workitem.wsht.AsyncWSHumanTaskHandler;
import org.jbpm.process.workitem.wsht.async.WSHumanTaskHandlerBaseAsyncTest;
import org.jbpm.task.TaskService;
import org.jbpm.task.TestStatefulKnowledgeSession;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.test.impl.TestTaskServer;

public class WSHumanTaskHandlerAsyncTest extends WSHumanTaskHandlerBaseAsyncTest {

    private TaskServer server;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        server = startServer(taskService);
        while (!server.isRunning()) {
            Thread.sleep( 50 );
        }
        
        TaskClient client = new TaskClient(createTestTaskClientConnector("client 1", (TestTaskServer) server));
        client.connect();
        setClient(client);
        
        TestStatefulKnowledgeSession ksession = new TestStatefulKnowledgeSession();
        AsyncWSHumanTaskHandler handler = new AsyncWSHumanTaskHandler(getClient(), ksession);
        handler.setClient(client);
        setHandler(handler);
        setSession(ksession);
    }


    protected void tearDown() throws Exception {
        ((AsyncWSHumanTaskHandler) getHandler()).dispose();
        getClient().disconnect();
        server.stop();
        super.tearDown();
    }
}
