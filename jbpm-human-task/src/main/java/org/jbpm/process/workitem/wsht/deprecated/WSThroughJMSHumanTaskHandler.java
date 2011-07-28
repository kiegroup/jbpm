package org.jbpm.process.workitem.wsht.deprecated;

import java.lang.reflect.Field;

import javax.naming.InitialContext;
import javax.naming.NamingException;

//import org.drools.SystemEventListenerFactory;
//import org.jbpm.process.workitem.wsht.AsyncWSHumanTaskHandler;
//import org.jbpm.task.service.impl.TaskServiceClientAsyncImpl;
//import org.jbpm.task.service.jms.JMSTaskClientConnector;
//import org.jbpm.task.service.jms.JMSTaskClientHandler;
//import org.jbpm.task.service.jms.WSHumanTaskJMSProperties;

/*
 * This class it's not being used or tested in this project
 */
@Deprecated
public class WSThroughJMSHumanTaskHandler { //extends AsyncWSHumanTaskHandler {

//	public WSThroughJMSHumanTaskHandler() {
//		super();
//	}
//	
//	@Override
//	public void connect() {
//		try {
//			final Field field = AsyncWSHumanTaskHandler.class.getDeclaredField("client");
//			TaskServiceClientAsyncImpl client = (TaskServiceClientAsyncImpl) field.get(this);
//			if (client == null) {
//				client = new TaskServiceClientAsyncImpl(new JMSTaskClientConnector(
//						"org.jbpm.process.workitem.wsht.WSThroughJMSHumanTaskHandler",
//						new JMSTaskClientHandler(SystemEventListenerFactory
//								.getSystemEventListener()),
//						WSHumanTaskJMSProperties.getInstance().getProperties(),
//						new InitialContext(WSHumanTaskJMSProperties.getInstance().getProperties())));
//				field.set(this, client);
//				boolean connected = client.connect();
//				if (!connected) {
//					throw new IllegalArgumentException("Could not connect to the task client");
//				}
//			}
//			super.connect();
//		} catch (NoSuchFieldException e) {
//			throw new RuntimeException("Problem configuring the human task connector", e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException("Problem accessing the human task connector", e);
//		} catch (NamingException e) {
//			throw new RuntimeException("Problem accesing the JNDI directory", e);
//		}
//	}
}
