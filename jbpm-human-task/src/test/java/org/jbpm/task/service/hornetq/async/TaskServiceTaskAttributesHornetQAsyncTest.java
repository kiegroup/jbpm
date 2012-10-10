package org.jbpm.task.service.hornetq.async;

import org.drools.SystemEventListenerFactory;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.base.async.TaskServiceTaskAttributesBaseAsyncTest;
import org.jbpm.task.service.hornetq.HornetQTaskClientConnector;
import org.jbpm.task.service.hornetq.HornetQTaskClientHandler;
import org.jbpm.task.service.hornetq.HornetQTaskServer;

public class TaskServiceTaskAttributesHornetQAsyncTest extends TaskServiceTaskAttributesBaseAsyncTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		server = new HornetQTaskServer(taskService, 5446);
		logger.debug("Waiting for the HornetQTask Server to come up");
        try {
            startTaskServerThread(server, false);
        } catch (Exception e) {
            startTaskServerThread(server, true);
        }

		client = new TaskClient(new HornetQTaskClientConnector("client 1",
								new HornetQTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
		client.connect("127.0.0.1", 5446);
	}

}
