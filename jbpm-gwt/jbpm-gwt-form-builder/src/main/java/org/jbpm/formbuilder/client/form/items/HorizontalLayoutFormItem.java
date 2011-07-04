package org.jbpm.formbuilder.client.form.items;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HorizontalPanelRepresentation;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalLayoutFormItem extends LayoutFormItem {

    private HorizontalPanel panel = new HorizontalPanel();

    public HorizontalLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public HorizontalLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        panel.setBorderWidth(1);
        add(panel);
        setSize("90px", "30px");
        panel.setSize(getWidth(), getHeight());
    }
    
    private Integer borderWidth;
    private Integer spacing;
    private String cssClassName;
    private String horizontalAlignment;
    private String verticalAlignment;
    private String title;
    private String id;
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("borderWidth", borderWidth);
        formItemPropertiesMap.put("height", getHeight());
        formItemPropertiesMap.put("width", getWidth());
        formItemPropertiesMap.put("spacing", spacing);
        formItemPropertiesMap.put("cssClassName", cssClassName);
        formItemPropertiesMap.put("horizontalAlignment", horizontalAlignment);
        formItemPropertiesMap.put("verticalAlignment", verticalAlignment);
        formItemPropertiesMap.put("title", title);
        formItemPropertiesMap.put("id", id);
        return formItemPropertiesMap;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.borderWidth = extractInt(asPropertiesMap.get("borderWidth"));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.spacing = extractInt(asPropertiesMap.get("spacing"));
        this.cssClassName = extractString(asPropertiesMap.get("cssClassName"));
        this.horizontalAlignment = extractString(asPropertiesMap.get("horizontalAlignment"));
        this.verticalAlignment = extractString(asPropertiesMap.get("verticalAlignment"));
        this.title = extractString(asPropertiesMap.get("title"));
        this.id = extractString(asPropertiesMap.get("id"));
        
        populate(this.panel);
    }

    private void populate(HorizontalPanel panel) {
        if (this.borderWidth != null) {
            panel.setBorderWidth(this.borderWidth);
        }
        if (this.getHeight() != null && !"".equals(this.getHeight())) {
            panel.setHeight(this.getHeight());
        }
        if (this.getWidth() != null && !"".equals(this.getWidth())) {
            panel.setWidth(this.getWidth());
        }
        if (this.spacing != null) {
            panel.setSpacing(this.spacing);
        }
        if (this.cssClassName != null) {
            panel.setStyleName(this.cssClassName);
        }
        //panel.setHorizontalAlignment(HorizontalAlignmentConstant.startOf(Direction.valueOf(horizontalAlignment))); TODO
        if (this.title != null) {
            panel.setTitle(this.title);
        }
    }
    
    @Override
    public Panel getPanel() {
        return panel;
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        HorizontalPanelRepresentation rep = super.getRepresentation(new HorizontalPanelRepresentation());
        rep.setBorderWidth(this.borderWidth);
        rep.setCssClassName(this.cssClassName);
        rep.setHorizontalAlignment(this.horizontalAlignment);
        rep.setId(this.id);
        rep.setSpacing(this.spacing);
        rep.setTitle(this.title);
        rep.setVerticalAlignment(this.verticalAlignment);
        for (FBFormItem item : getItems()) {
            rep.addItem(item.getRepresentation());
        }
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof HorizontalPanelRepresentation)) {
            throw new FormBuilderException("rep should be of type HorizontalPanelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        HorizontalPanelRepresentation hrep = (HorizontalPanelRepresentation) rep;
        this.borderWidth = hrep.getBorderWidth();
        this.cssClassName = hrep.getCssClassName();
        this.horizontalAlignment = hrep.getHorizontalAlignment();
        this.id = hrep.getId();
        this.spacing = hrep.getSpacing();
        this.title = hrep.getTitle();
        this.verticalAlignment = hrep.getVerticalAlignment();
        this.panel.clear();
        super.getItems().clear();
        populate(this.panel);
        if (hrep.getItems() != null) {
            for (FormItemRepresentation item : hrep.getItems()) {
                add(super.createItem(item));
            }
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        HorizontalLayoutFormItem clone = new HorizontalLayoutFormItem(getFormEffects());
        clone.borderWidth = this.borderWidth;
        clone.cssClassName = this.cssClassName;
        clone.setHeight(this.getHeight());
        clone.horizontalAlignment = this.horizontalAlignment;
        clone.id = this.id;
        clone.spacing = this.spacing;
        clone.title = this.title;
        clone.verticalAlignment = this.verticalAlignment;
        clone.setWidth(this.getWidth());
        clone.populate(clone.panel);
        for (FBFormItem item : getItems()) {
            clone.add(item.cloneItem());
        }
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        HorizontalPanel hp = new HorizontalPanel();
        populate(hp);
        for (FBFormItem item : getItems()) {
            hp.add(item.cloneDisplay());
        }
        return hp;
    }
}
