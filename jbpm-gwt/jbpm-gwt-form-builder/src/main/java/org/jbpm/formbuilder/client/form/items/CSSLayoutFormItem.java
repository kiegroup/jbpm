package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CSSPanelRepresentation;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class CSSLayoutFormItem extends LayoutFormItem {

    private FlowPanel panel = new FlowPanel() {
        @Override
        public boolean remove(Widget w) {
            if (w instanceof FBFormItem) {
                removeItem((FBFormItem) w);
            }
            return super.remove(w);
        }
    };
    private StyleElement style = Document.get().createStyleElement(); 

    private String cssStylesheetUrl;
    private String cssClassName;
    private String id;

    public CSSLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public CSSLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        setSize("190px", "90px");
        panel.setSize(getWidth(), getHeight());
        this.style.setPropertyString("language", "text/css");
        panel.getElement().insertFirst(this.style);
        add(panel);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cssStylesheetUrl", this.cssStylesheetUrl);
        map.put("cssClassName", this.cssClassName);
        map.put("id", this.id);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> map) {
        this.cssStylesheetUrl = extractString(map.get("cssStylesheetUrl"));
        this.cssClassName = extractString(map.get("cssClassName"));
        this.id = extractString(map.get("id"));
        populate(this.panel, this.style);
    }

    private void populate(FlowPanel panel, StyleElement style) {
        if (getHeight() != null) {
            panel.setHeight(getHeight());
        }
        if (this.cssClassName != null) {
            panel.setStyleName(cssClassName);
        }
        if (getWidth() != null) {
            panel.setWidth(getWidth());
        }
        if (this.cssStylesheetUrl != null) {
            style.setPropertyString("src", this.cssStylesheetUrl);
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        CSSPanelRepresentation rep = super.getRepresentation(new CSSPanelRepresentation());
        List<FormItemRepresentation> items = new ArrayList<FormItemRepresentation>();
        for (FBFormItem item : getItems()) {
            items.add(item.getRepresentation());
        }
        rep.setItems(items);
        rep.setId(this.id);
        rep.setCssClassName(this.cssClassName);
        rep.setCssStylesheetUrl(this.cssStylesheetUrl);
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof CSSPanelRepresentation)) {
            throw new FormBuilderException("rep should be of type CSSPanelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        CSSPanelRepresentation crep = (CSSPanelRepresentation) rep;
        this.cssClassName = crep.getCssClassName();
        this.id = crep.getId();
        super.getItems().clear();
        populate(this.panel, this.style);
        if (crep.getItems() != null) {
            for (FormItemRepresentation item : crep.getItems()) {
                add(super.createItem(item));
            }
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        CSSLayoutFormItem clone = super.cloneItem(new CSSLayoutFormItem(getFormEffects()));
        clone.cssClassName = this.cssClassName;
        clone.cssStylesheetUrl = this.cssStylesheetUrl;
        clone.id = this.id;
        clone.populate(clone.panel, clone.style);
        for (FBFormItem item : getItems()) {
            clone.add(item.cloneItem());
        }
        return clone;
    }

    @Override
    public Widget cloneDisplay() {
        FlowPanel fp = new FlowPanel();
        fp.getElement().insertFirst(this.style.cloneNode(false));
        populate(fp, this.style);
        for (FBFormItem item : getItems()) {
            fp.add(item.cloneDisplay());
        }
        return fp;
    }
    
    @Override
    public Panel getPanel() {
        return this.panel;
    }
    
    @Override
    public boolean add(FBFormItem item) {
        panel.add(item);
        return super.add(item);
    }
}
