package org.jbpm.formbuilder.client.notification;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jbpm.formapi.client.CommonGlobals;
import org.jbpm.formapi.client.bus.ui.NotificationEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

public class NotificationsPresenterTest extends TestCase {

	private NotificationsView view;
	private EventBus bus;
	private CommonGlobals cg;
	
	@Before
	@Override
	protected void setUp() throws Exception {
		view = EasyMock.createMock(NotificationsView.class);
		bus = new SimpleEventBus();
		cg = CommonGlobals.getInstance();
		cg.registerEventBus(bus);
	}
	
	@After
	@Override
	protected void tearDown() throws Exception {
		view = null;
	}
	
	@Test
	public void testNotificationStartup() throws Exception {
		EasyMock.replay(view);
		new NotificationsPresenter(view);
		EasyMock.verify(view);
	}

	@Test
	public void testNotificationEvent() throws Exception {
		
		String message1 = "info message";
		String color1 = "greenNotification";

		String message2 = "warn message";
		String color2 = "orangeNotification";
		NotificationEvent.Level level2 = NotificationEvent.Level.WARN;
		
		String message3 = "error message";
		String color3 = "redNotification";
		NotificationEvent.Level level3 = NotificationEvent.Level.ERROR;
		Throwable throwable3 = new NullPointerException();
	
		EasyMock.expect(view.getColorCss(EasyMock.eq(NotificationEvent.Level.INFO.toString()))).
			andReturn(color1).once();
		EasyMock.expect(view.getColorCss(EasyMock.eq(level2.toString()))).
			andReturn(color2).once();
		EasyMock.expect(view.getColorCss(EasyMock.eq(level3.toString()))).
			andReturn(color3).once();
		
		view.append(EasyMock.eq(color1), EasyMock.eq(message1), EasyMock.isNull(Throwable.class));
		EasyMock.expectLastCall().once();
		view.append(EasyMock.eq(color2), EasyMock.eq(message2), EasyMock.isNull(Throwable.class));
		EasyMock.expectLastCall().once();
		view.append(EasyMock.eq(color3), EasyMock.eq(message3), EasyMock.eq(throwable3));
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(view);
		new NotificationsPresenter(view);
		bus.fireEvent(new NotificationEvent(message1));
		bus.fireEvent(new NotificationEvent(level2, message2));
		bus.fireEvent(new NotificationEvent(level3, message3, throwable3));
		EasyMock.verify(view);
	}
}
