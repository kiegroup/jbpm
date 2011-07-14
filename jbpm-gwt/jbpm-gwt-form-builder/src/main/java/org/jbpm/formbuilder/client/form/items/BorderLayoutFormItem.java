package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.BorderPanelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.BorderPanelRepresentation.Position;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class BorderLayoutFormItem extends LayoutFormItem {

	private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
	
	private Map<Position, FBFormItem> locations = new HashMap<Position, FBFormItem>();
	
	private Position currentPosition = null;
	
	private Grid grid = new Grid(1,1) {
        @Override
        public boolean remove(Widget widget) {
            if (widget instanceof FBFormItem) {
            	super.remove(widget);
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
	}

	protected void setDropPosition(Position position) {
		this.currentPosition = position;
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
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("width", getWidth());
		map.put("height", getHeight());
		return map;
	}

	@Override
	public void saveValues(Map<String, Object> asPropertiesMap) {
		setHeight(extractString(asPropertiesMap.get("height")));
		setWidth(extractString(asPropertiesMap.get("width")));
		populate(this.grid);
	}

	private void populate(Grid myGrid) {
		if (getHeight() != null && !"".equals(getHeight())) {
			this.grid.setHeight(getHeight());
		}
		if (getWidth() != null && !"".equals(getWidth())) {
			this.grid.setWidth(getWidth());
		}
	}

	@Override
	public FormItemRepresentation getRepresentation() {
		BorderPanelRepresentation rep = super.getRepresentation(new BorderPanelRepresentation());
		for (Map.Entry<Position, FBFormItem> entry : this.locations.entrySet()) {
			Position key = entry.getKey();
			FormItemRepresentation value = entry.getValue().getRepresentation();
			rep.putItem(key, value);
		}
		return rep;
	}
	
	@Override
	public void populate(FormItemRepresentation rep) throws FormBuilderException {
		if (!(rep instanceof BorderPanelRepresentation)) {
            throw new FormBuilderException("rep should be of type BorderPanelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        BorderPanelRepresentation brep = (BorderPanelRepresentation) rep;
        Map<Position, FormItemRepresentation> repItems = brep.getItems();
        if (repItems != null) {
        	for (Map.Entry<Position, FormItemRepresentation> entry : repItems.entrySet()) {
        		Position key = entry.getKey();
        		FBFormItem value = super.createItem(entry.getValue());
        		this.currentPosition = key;
        		this.add(value);
        	}
        }
	}

	@Override
	public FBFormItem cloneItem() {
		BorderLayoutFormItem clone = super.cloneItem(new BorderLayoutFormItem(getFormEffects()));
		clone.currentPosition = this.currentPosition;
		clone.grid = (Grid) cloneDisplay();
		clone.locations = new HashMap<Position, FBFormItem>(this.locations);
		return clone;
	}

	@Override
	public Widget cloneDisplay() {
		int rows = this.grid.getRowCount();
		int columns = this.grid.getColumnCount();
		Grid g = new Grid(rows, columns);
        populate(g);
        for (int index = 0; index < columns * rows; index++) {
            int column = index%columns;
            int row = index/columns;
            FBFormItem item = (FBFormItem) this.grid.getWidget(row, column);
            if (item != null) {
                g.setWidget(row, column, item.cloneDisplay());
            }
        }
        return g;
	}

	@Override
	public Panel getPanel() {
		return grid;
	}
	
	@Override
	public boolean add(FBFormItem item) {
		currentPosition = obtainPosition(item.getDesiredX(), item.getDesiredY());
		int row = 0;
		int col = 0;
		switch (currentPosition) {
		case NORTH:
			ensureRows();
			col = getMiddleColumn();
			row = 0;
			break;
		case NORTHEAST:
			ensureRows();
			ensureColumns();
			col = 0;
			row = 0;
			break;
		case EAST:
			ensureColumns();
			row = getMiddleRow();
			col = 2;
			break;
		case SOUTHEAST:
			ensureColumns();
			ensureRows();
			row = 2;
			col = 2;
			break;
		case SOUTH:
			ensureRows();
			row = 2;
			col = getMiddleColumn();
			break;
		case SOUTHWEST:
			ensureRows();
			ensureColumns();
			row = 2;
			col = 0;
			break;
		case WEST:
			ensureColumns();
			row = getMiddleRow();
			col = 0;
			break;
		case NORTHWEST:
			ensureColumns();
			ensureRows();
			col = 0;
			row = 0;
			break;
		default: //CENTER
			row = getMiddleRow();
			col = getMiddleColumn();
			break;
		}
		if (locations.get(currentPosition) == null) {
			grid.setWidget(row, col, item);
			locations.put(currentPosition, item);
			currentPosition = null;
			return super.add(item);
		} else {
			bus.fireEvent(new NotificationEvent(NotificationEvent.Level.WARN, 
            	"Border layout position already pouplated! Use different coordinates"));
			return false;
		}
	}

	private int getMiddleRow() {
		int row = 0;
		if (grid.getRowCount() == 3) {
			row = 1;
		}
		return row;
	}

	private int getMiddleColumn() {
		int col = 0;
		if (grid.getColumnCount() == 3) {
			col = 1;
		}
		return col;
	}
	private void ensureRows() {
		if (grid.getRowCount() < 3) {
			grid.resizeRows(3);
			for (int i = 0; i < 2; i++) {
				if (grid.getColumnCount() > i) {
					Widget widget = grid.getWidget(0, i);
					if (widget != null) {
						grid.remove(widget);
						grid.setWidget(1, i, widget);
					}
				}
			}
			grid.setSize(getWidth(), getHeight());
		}
	}
	
	private void ensureColumns() {
		if (grid.getColumnCount() < 3) {
			grid.resizeColumns(3);
			for (int i = 0; i < 2; i++) {
				if (grid.getRowCount() > i) {
					Widget widget = grid.getWidget(i, 0);
					if (widget != null) {
						grid.remove(widget);
						grid.setWidget(i, 1, widget);
					}
				}
			}
			grid.setSize(getWidth(), getHeight());
		}
	}

	@Override
	public boolean remove(Widget child) {
        boolean removed = false;
        if (child instanceof FBFormItem) {
            Position pos = null;
            for (Map.Entry<Position, FBFormItem> entry : this.locations.entrySet()) {
            	if (entry.getValue().equals(child)) {
            		pos = entry.getKey();
            		break;
            	}
            }
            this.locations.remove(pos);
            removeItem((FBFormItem) child);
        } else {
            removed = super.remove(child);
        }
        return removed;
	}
	
}
