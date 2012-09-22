package org.jbpm.task.service.mina;

import org.drools.SystemEventListenerFactory;
import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.TaskServiceTaskAttributesBaseTest;

public class TaskServiceTaskAttributesMinaTest extends TaskServiceTaskAttributesBaseTest {

	@Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new MinaTaskServer( taskService );
        Thread thread = new Thread( server );
        thread.start();
        logger.debug("Waiting for the MinaTask Server to come up");
        while (!server.isRunning()) {

        	Thread.sleep( 50 );
        }
        client = new TaskClient(new MinaTaskClientConnector("client 1",
                                new MinaTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        client.connect("127.0.0.1", 9123);
    }

}
