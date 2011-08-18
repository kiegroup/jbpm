package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.PhantomPanel;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TabbedPanelRepresentation;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class TabbedLayoutFormItem extends LayoutFormItem {

    private final List<FlowLayoutFormItem> tabs = new ArrayList<FlowLayoutFormItem>();
    private final List<LabelFormItem> titles = new ArrayList<LabelFormItem>();
    private String cssClassName;
    private String id;
    
    private TabLayoutPanel panel = new TabLayoutPanel(21, Unit.PX) {
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
        LabelFormItem tab1 = new LabelFormItem(getFormEffects());
        tab1.getLabel().setText("Tab 1");
        panel.add(new FlowLayoutFormItem(getFormEffects()), tab1);
        LabelFormItem tab2 = new LabelFormItem(getFormEffects());
        tab2.getLabel().setText("Tab 2");
        panel.add(new FlowLayoutFormItem(getFormEffects()), tab2);
        LabelFormItem tab3 = new LabelFormItem(getFormEffects());
        tab3.getLabel().setText("Tab 3");
        panel.add(new FlowLayoutFormItem(getFormEffects()), tab3);
        setSize("300px", "400px");
        panel.setSize("300px", "400px");
        add(panel);
    }

    public TabbedLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    @Override
    public void replacePhantom(FBFormItem item) {
        int selectedIndex = panel.getSelectedIndex();
        Widget tabWidget = panel.getWidget(selectedIndex);
        FlowLayoutFormItem tab = (FlowLayoutFormItem) tabWidget;
        tab.replacePhantom(item);
    }
    
    @Override
    public void add(PhantomPanel phantom, int x, int y) {
        int selectedIndex = panel.getSelectedIndex();
        Widget widget = panel.getWidget(selectedIndex);
        FlowLayoutFormItem tab = (FlowLayoutFormItem) widget;
        tab.add(phantom, x, y);
    }

    @Override
    public HasWidgets getPanel() {
        return this.panel;
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("height", getHeight());
        map.put("width", getWidth());
        map.put("cssClassName", this.cssClassName);
        map.put("id", this.id);
        map.put("numberOfTabs", panel.getWidgetCount());
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        String height = extractString(asPropertiesMap.get("height"));
        if (height != null && !"".equals(height)) {
            setHeight(height);
        }
        String width = extractString(asPropertiesMap.get("width"));
        if (width != null && !"".equals(width)) {
            setWidth(width);
        }
        this.cssClassName = extractString(asPropertiesMap.get("cssClassName"));
        this.id = extractString(asPropertiesMap.get("id"));
        Integer numberOfTabs = extractInt(asPropertiesMap.get("numberOfTabs"));
        
        if (numberOfTabs > panel.getWidgetCount()) {
            int qtyToAdd = numberOfTabs - panel.getWidgetCount();
            while (qtyToAdd > 0) {
                LabelFormItem label = new LabelFormItem(getFormEffects());
                FlowLayoutFormItem flow = new FlowLayoutFormItem(getFormEffects());
                label.getLabel().setText("Tab " + panel.getWidgetCount());
                panel.add(flow, label);
                qtyToAdd--;
            }
        } else if (numberOfTabs < panel.getWidgetCount()) {
            while (numberOfTabs != panel.getWidgetCount()) {
                panel.remove(panel.getWidgetCount() - 1);
                numberOfTabs--;
            }
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        TabbedPanelRepresentation trep = super.getRepresentation(new TabbedPanelRepresentation());
        trep.setCssClassName(this.cssClassName);
        trep.setId(this.id);
        for (int index = 0; index < titles.size(); index++) {
            FlowLayoutFormItem tabContent = null;
            if (index < tabs.size()) {
                tabContent = tabs.get(index);
            }
            if (tabContent == null) {
                tabContent = new FlowLayoutFormItem(getFormEffects());
            }
            String tabTitle = titles.get(index).getLabel().getText();
            trep.putTab(index, tabTitle, tabContent.getRepresentation());
        }
        return trep;
    }
    
    private void populate(TabLayoutPanel panel) {
        panel.setHeight(getHeight());
        panel.setWidth(getWidth());
        panel.clear();
        for (int index = 0; index < this.titles.size() && index < this.tabs.size(); index++) {
            FlowLayoutFormItem flow = this.tabs.get(index);
            LabelFormItem label = this.titles.get(index);
            if (flow != null && label != null) {
                panel.add(flow.cloneItem(), label.cloneItem());
            }
        }
    }

    @Override
    public FBFormItem cloneItem() {
        TabbedLayoutFormItem clone = new TabbedLayoutFormItem(getFormEffects());
        clone.id = this.id;
        clone.cssClassName = this.cssClassName;
        for (LabelFormItem label : this.titles) {
            clone.titles.add((LabelFormItem) label.cloneItem());
        }
        for (FlowLayoutFormItem flow : this.tabs) {
            clone.tabs.add((FlowLayoutFormItem) flow.cloneItem());
        }
        populate(clone.panel);
        return clone;
    }

    @Override
    public boolean add(FBFormItem item) {
        int index = panel.getSelectedIndex();
        Widget widget = panel.getWidget(index);
        FlowLayoutFormItem tab = (FlowLayoutFormItem) widget;
        return tab.add(item);
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof TabbedPanelRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "TabbedPanelRepresentation"));
        }
        super.populate(rep);
        TabbedPanelRepresentation trep = (TabbedPanelRepresentation) rep;
        this.cssClassName = trep.getCssClassName();
        this.id = trep.getId();
        this.titles.clear();
        for (TabbedPanelRepresentation.IndexedString title : trep.getTabTitles()) {
            LabelFormItem label = new LabelFormItem(getFormEffects());
            label.getLabel().setText(title.getString());
            FormItemRepresentation subRep = trep.getTabContents().get(title);
            FBFormItem subItem = FBFormItem.createItem(subRep);
            this.tabs.add((FlowLayoutFormItem) subItem);
            this.titles.add(label);
        }
        populate(this.panel);
    }

    @Override
    public Widget cloneDisplay() {
        return ((TabbedLayoutFormItem)cloneItem()).panel;
    }
}
