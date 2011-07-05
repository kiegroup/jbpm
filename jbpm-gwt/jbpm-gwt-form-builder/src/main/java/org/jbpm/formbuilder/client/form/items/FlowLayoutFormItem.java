package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.FlowPanelRepresentation;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class FlowLayoutFormItem extends LayoutFormItem {

	private FlowPanel panel = new FlowPanel();
	
	private String cssClassName;
	private String id;
	
	public FlowLayoutFormItem() {
		this(new ArrayList<FBFormEffect>());
	}
	
	public FlowLayoutFormItem(List<FBFormEffect> effects) {
		super(effects);
		setSize("190px", "90px");
        panel.setSize(getWidth(), getHeight());
		add(panel);
	}

	private void populate(FlowPanel panel) {
		if (getHeight() != null) {
			panel.setHeight(getHeight());
		}
		if (this.cssClassName != null) {
			panel.setStyleName(cssClassName);
		}
		if (getWidth() != null) {
			panel.setWidth(getWidth());
		}
	}
	
	@Override
	public Map<String, Object> getFormItemPropertiesMap() {
		Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("height", getHeight());
        formItemPropertiesMap.put("width", getWidth());
        formItemPropertiesMap.put("cssClassName", this.cssClassName);
        formItemPropertiesMap.put("id", id);
        return formItemPropertiesMap;
	}

	@Override
	public void saveValues(Map<String, Object> asPropertiesMap) {
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.cssClassName = extractString(asPropertiesMap.get("cssClassName"));
        this.id = extractString(asPropertiesMap.get("id"));
        
        populate(this.panel);
	}

	@Override
	public FormItemRepresentation getRepresentation() {
		FlowPanelRepresentation rep = super.getRepresentation(new FlowPanelRepresentation());
		List<FormItemRepresentation> items = new ArrayList<FormItemRepresentation>();
		for (FBFormItem item : getItems()) {
			items.add(item.getRepresentation());
		}
		rep.setItems(items);
		rep.setId(this.id);
		rep.setCssClassName(this.cssClassName);
		return rep;
	}
	
	@Override
	public void populate(FormItemRepresentation rep) throws FormBuilderException {
		if (!(rep instanceof FlowPanelRepresentation)) {
            throw new FormBuilderException("rep should be of type FlowPanelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        FlowPanelRepresentation frep = (FlowPanelRepresentation) rep;
		this.cssClassName = frep.getCssClassName();
		this.id = frep.getId();
		super.getItems().clear();
        populate(this.panel);
        if (frep.getItems() != null) {
            for (FormItemRepresentation item : frep.getItems()) {
                add(super.createItem(item));
            }
        }
	}

	@Override
	public FBFormItem cloneItem() {
        FlowLayoutFormItem clone = super.cloneItem(new FlowLayoutFormItem(getFormEffects()));
        clone.cssClassName = this.cssClassName;
        clone.id = this.id;
        clone.populate(clone.panel);
        for (FBFormItem item : getItems()) {
            clone.add(item.cloneItem());
        }
        return clone;
	}

	@Override
	public Widget cloneDisplay() {
        FlowPanel fp = new FlowPanel();
        populate(fp);
        for (FBFormItem item : getItems()) {
            fp.add(item.cloneDisplay());
        }
        return fp;
	}

	@Override
	public Panel getPanel() {
		return this.panel;
	}
}
