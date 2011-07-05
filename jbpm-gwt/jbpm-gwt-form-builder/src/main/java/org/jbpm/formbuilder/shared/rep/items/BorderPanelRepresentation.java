package org.jbpm.formbuilder.shared.rep.items;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class BorderPanelRepresentation extends FormItemRepresentation {

	public static enum Position {
		SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST, CENTER;
	}
	
	private Map<Position, FormItemRepresentation> items = new HashMap<Position, FormItemRepresentation>();
	
	public BorderPanelRepresentation() {
		super("borderPanel");
	}

	public Map<Position, FormItemRepresentation> getItems() {
		return items;
	}

	public void setItems(Map<Position, FormItemRepresentation> items) {
		this.items = items;
	}

	public FormItemRepresentation putItem(Position key, FormItemRepresentation value) {
		return items.put(key, value);
	}
	
	
}
