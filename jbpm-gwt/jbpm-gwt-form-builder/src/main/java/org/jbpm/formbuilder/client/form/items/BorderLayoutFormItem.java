package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class BorderLayoutFormItem extends LayoutFormItem {

	enum Position {
		SOUTH, SOUTHWEST, WEST, NORTHWEST, NORTH, NORTHEAST, EAST, SOUTHEAST, CENTER;
	}
	
	class PositionWrapper {
		private Position position;
		private FBFormItem item;

		public Position getPosition() {
			return position;
		}
		
		public void setPosition(Position position) {
			this.position = position;
		}
		
		public FBFormItem getItem() {
			return item;
		}
		
		public void setItem(FBFormItem item) {
			this.item = item;
		}
	}
	
	private Grid grid = new Grid(1,1) {
        @Override
        public boolean remove(Widget widget) {
            if (widget instanceof FBFormItem) {
                return BorderLayoutFormItem.this.remove(widget);
            } else {
                return super.remove(widget);
            }
        }
	};

	public BorderLayoutFormItem() {
	    this(new ArrayList<FBFormEffect>());
    }
	
	public BorderLayoutFormItem(List<FBFormEffect> formEffects) {
		super(formEffects);
		add(grid);
		grid.setSize("180px", "180px");
		setSize("180px", "180px");
		grid.addHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				Position pos = obtainPosition(event.getX(), event.getY());
				clearHighlihgting();
				highlight(pos);
				
			}
		}, MouseOverEvent.getType());
		grid.addHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				clearHighlihgting();
			}
		}, MouseOutEvent.getType());
		grid.addHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				Position pos = obtainPosition(event.getX(), event.getY());
				clearHighlihgting();
				highlight(pos);
			}
		}, MouseMoveEvent.getType());
		grid.addHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent event) {
				Position pos = obtainPosition(event.getX(), event.getY());
				clearHighlihgting();
				// TODO on decided position, add element
			}
		}, MouseUpEvent.getType());
	}
	
	protected void clearHighlihgting() {
		//TODO clear all highlighting of positions
	}
	
	protected void highlight(Position position) {
		if (position != null) {
			//TODO highlight correct position
		}
	}
	
	protected Position obtainPosition(int x, int y) {
		int xpos = x - grid.getAbsoluteLeft(); 
		int width = grid.getOffsetWidth();
		int ypos = y - grid.getAbsoluteTop();
		int height = grid.getOffsetHeight();
		boolean left = width / 3 > xpos;
		boolean right = width * 2 / 3 < xpos;
		boolean bottom = height * 2 / 3 < ypos;
		boolean top = height / 3 > ypos;
		if (!left && !right && !bottom && !top) {
			return Position.CENTER;
		} else if (!left && !right && top) {
			return Position.NORTH;
		} else if (!left && !right && bottom) {
			return Position.SOUTH;
		} else if (!bottom && !top && right) {
			return Position.EAST;
		} else if (!bottom && !top && left) {
			return Position.WEST;
		} else if (top && right) {
			return Position.NORTHEAST;
		} else if (bottom && right) {
			return Position.SOUTHEAST;
		} else if (top && left) {
			return Position.NORTHWEST;
		} else if (bottom && left) {
			return Position.SOUTHWEST;
		} else {
			return null;
		}
	}
	
	@Override
	public Map<String, Object> getFormItemPropertiesMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveValues(Map<String, Object> asPropertiesMap) {
		// TODO Auto-generated method stub

	}

	@Override
	public FormItemRepresentation getRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void populate(FormItemRepresentation rep)
			throws FormBuilderException {
		// TODO Auto-generated method stub
		super.populate(rep);
	}

	@Override
	public FBFormItem cloneItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Widget cloneDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Panel getPanel() {
		return grid;
	}
	
	@Override
	public boolean add(FBFormItem item) {
		// TODO Auto-generated method stub
		return super.add(item);
	}

	@Override
	public boolean remove(Widget child) {
        boolean removed = false;
        if (child instanceof FBFormItem) {
        	
            for (int i = 0; i < grid.getRowCount(); i++) {
                for (int j = 0; j < grid.getColumnCount(); j++) {
                    if (grid.getWidget(i, j) != null && grid.getWidget(i, j).equals(child)) {
                        removed = super.remove(child);
                        ////WARN dom used: seems the only way of fixing deleted cell bug
                        grid.getWidget(i, j).getElement().getParentElement().setInnerHTML("&nbsp;");
                        break;
                    }
                }
            }
        } else {
            removed = super.remove(child);
        }
        return removed;
	}
	
}
