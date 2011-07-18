package org.jbpm.process.workitem.wsht;

import java.lang.reflect.Field;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.drools.SystemEventListenerFactory;
import org.jbpm.task.service.AsyncTaskClientImpl;
import org.jbpm.task.service.jms.JMSTaskClientConnector;
import org.jbpm.task.service.jms.JMSTaskClientHandler;
import org.jbpm.task.service.jms.WSHumanTaskJMSProperties;


public class WSThroughJMSHumanTaskHandler extends AsyncWSHumanTaskHandler {

	public WSThroughJMSHumanTaskHandler() {
		super();
	}
	
	@Override
	public void connect() {
		try {
			final Field field = AsyncWSHumanTaskHandler.class.getDeclaredField("client");
			AsyncTaskClientImpl client = (AsyncTaskClientImpl) field.get(this);
			if (client == null) {
				client = new AsyncTaskClientImpl(new JMSTaskClientConnector(
						"org.jbpm.process.workitem.wsht.WSThroughJMSHumanTaskHandler",
						new JMSTaskClientHandler(SystemEventListenerFactory
								.getSystemEventListener()),
						WSHumanTaskJMSProperties.getInstance().getProperties(),
						new InitialContext(WSHumanTaskJMSProperties.getInstance().getProperties())));
				field.set(this, client);
				boolean connected = client.connect();
				if (!connected) {
					throw new IllegalArgumentException("Could not connect to the task client");
				}
			}
			super.connect();
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Problem configuring the human task connector", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Problem accessing the human task connector", e);
		} catch (NamingException e) {
			throw new RuntimeException("Problem accesing the JNDI directory", e);
		}
	}
}
