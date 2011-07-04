package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public abstract class LayoutFormItem extends FBFormItem implements FBCompositeItem {

    private List<FBFormItem> items = new ArrayList<FBFormItem>();

    public LayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public LayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }
    
    public List<FBFormItem> getItems() {
        return items;
    }
    
    public void setItems(List<FBFormItem> items) {
        this.items = items;
    }

    public int size() {
        return items.size();
    }

    public Iterator<FBFormItem> formItemIterator() {
        return items.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        if (w instanceof FBFormItem) {
            this.items.remove((FBFormItem) w);
        }
        return super.remove(w);
    }
    
    public boolean remove(Object o) {
        return items.remove(o);
    }

    public FBFormItem get(int index) {
        return items.get(index);
    }

    public FBFormItem remove(int index) {
        return items.remove(index);
    }
    
    public ListIterator<FBFormItem> formItemListIterator() {
        return items.listIterator();
    }

    public boolean add(FBFormItem item) {
        return items.add(item);
    }

    public FBFormItem set(int index, FBFormItem element) {
        return items.set(index, element);
    }

    public abstract Panel getPanel();
    
    public Panel getUnderlyingLayout(int x, int y) {
        for (FBFormItem item : items) {
            if (item instanceof LayoutFormItem) {
                Panel newLayout = ((LayoutFormItem) item).getUnderlyingLayout(x, y);
                if (newLayout != null) {
                    return newLayout;
                }
            }
        }
        if (x > getAbsoluteLeft() && x < getAbsoluteLeft() + getOffsetWidth() &&
            y > getAbsoluteTop() && y < getAbsoluteTop() + getOffsetHeight()) {
            return getPanel();
        }
        return null;
    }
}
