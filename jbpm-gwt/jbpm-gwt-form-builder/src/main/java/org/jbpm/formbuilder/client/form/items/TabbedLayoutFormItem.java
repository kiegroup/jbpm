package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.PhantomPanel;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabbedLayoutFormItem extends LayoutFormItem {

    private TabLayoutPanel panel = new TabLayoutPanel(30, Unit.PX) {
        @Override
        public boolean remove(Widget widget) {
            if (widget instanceof FBFormItem) {
                TabbedLayoutFormItem.this.removeItem((FBFormItem) widget);
            }
            return super.remove(widget);
        };
    };
    
    public TabbedLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    public TabbedLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    @Override
    public void replacePhantom(FBFormItem item) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void add(PhantomPanel phantom, int x, int y) {
        // TODO Auto-generated method stub

    }

    @Override
    public HasWidgets getPanel() {
        return this.panel;
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        // TODO Auto-generated method stub
        /*panel.setHeight(height);
        panel.setWidth(width);
        panel.setStyleName(style);
        panel.add(widgetChildCouldBeALabel, flowPanelForTabContent);*/
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        // TODO Auto-generated method stub
        return null;
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

}
