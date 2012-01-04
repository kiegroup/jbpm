package org.jbpm.formbuilder.client.menu;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.jbpm.formapi.client.CommonGlobals;
import org.jbpm.formbuilder.client.MockHandlerRegistration;
import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemAddedHandler;
import org.jbpm.formbuilder.client.bus.MenuItemFromServerEvent;
import org.jbpm.formbuilder.client.bus.MenuItemFromServerHandler;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveHandler;
import org.junit.Test;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

public class MenuTest extends TestCase {

	@Test
	public void testMenuStartup() throws Exception {
		CommonGlobals cg = CommonGlobals.getInstance();
		
		EventBus mockBus = EasyMock.createMock(EventBus.class);
		PickupDragController mockdg = EasyMock.createMock(PickupDragController.class);
		MenuView mockView = EasyMock.createMock(MenuView.class);
		cg.registerEventBus(mockBus);
		cg.registerDragController(mockdg);
		
		mockView.startDropController(EasyMock.eq(mockdg));
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(mockBus.addHandler(
				EasyMock.same(MenuItemAddedEvent.TYPE), 
				EasyMock.isA(MenuItemAddedHandler.class))).
			andReturn(new MockHandlerRegistration()).once();
		EasyMock.expect(mockBus.addHandler(
				EasyMock.same(MenuItemRemoveEvent.TYPE),
				EasyMock.isA(MenuItemRemoveHandler.class))).
			andReturn(new MockHandlerRegistration()).once();
		EasyMock.expect(mockBus.addHandler(
				EasyMock.same(MenuItemFromServerEvent.TYPE), 
				EasyMock.isA(MenuItemFromServerHandler.class))).
			andReturn(new MockHandlerRegistration()).once();

		EasyMock.replay(mockBus, mockView, mockdg);
		new MenuPresenter(mockView);
		EasyMock.verify(mockBus, mockView, mockdg);
	}
}
