package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class HeaderFormItem extends FBFormItem {


    private final HTML header = new HTML("<h1>Header</h1>");
    
    private String id;
    private String name;
    private String cssClassName;

    public HeaderFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public HeaderFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(getHeader());
        setWidth("100%");
        setHeight("30px");
        getHeader().setSize(getWidth(), getHeight());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("id", id);
        formItemPropertiesMap.put("name", name);
        formItemPropertiesMap.put("width", getWidth());
        formItemPropertiesMap.put("height", getHeight());
        formItemPropertiesMap.put("cssClassName", cssClassName);
        return formItemPropertiesMap;
    }

    @Override
    public Widget createInplaceEditor() {
        final HorizontalPanel editPanel = new HorizontalPanel();
        editPanel.setBorderWidth(1);
        final TextBox textBox = new TextBox();
        textBox.setValue(getHeader().getText());
        textBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                setContent("<h1>" + textBox.getValue() + "</h1>");
                reset();
            }
        });
        editPanel.add(textBox);
        return editPanel;
    }

    @Override
    public void saveValues(Map<String, Object> propertiesMap) {
        this.id = extractString(propertiesMap.get("id"));
        this.name = extractString(propertiesMap.get("name"));
        setWidth(extractString(propertiesMap.get("width")));
        setHeight(extractString(propertiesMap.get("height")));
        this.cssClassName = extractString(propertiesMap.get("cssClassName"));
        populate(getHeader());
    }

    private void populate(HTML html) {
        if (getWidth() != null) {
            html.setWidth(getWidth());
        }
        if (this.getHeight() != null) {
            html.setHeight(getHeight());
        }
        if (this.cssClassName != null) {
            html.setStyleName(this.cssClassName);
        }
    }
    
    protected HTML getHeader() {
        return this.header;
    }
    
    protected void setContent(String html) {
        getHeader().setHTML(html);
    }
    
    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.header);
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        HeaderRepresentation rep = super.getRepresentation(new HeaderRepresentation());
        rep.setValue(this.header.getText());
        rep.setStyleClass(this.cssClassName);
        rep.setCssId(this.id);
        rep.setCssName(this.name);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof HeaderRepresentation)) {
            throw new FormBuilderException("rep should be of type LabelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        HeaderRepresentation hrep = (HeaderRepresentation) rep;
        this.header.setText("<h1>" + hrep.getValue() + "</h1>");
        this.cssClassName = hrep.getCssName();
        this.id = hrep.getCssId();
        populate(this.header);
    }
    
    @Override
    public FBFormItem cloneItem() {
        HeaderFormItem clone = super.cloneItem(new HeaderFormItem(getFormEffects()));
        clone.cssClassName = this.cssClassName;
        clone.id = this.id;
        clone.name = this.name;
        clone.setContent(this.header.getHTML());
        clone.populate(this.header);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        HTML html = new HTML(this.header.getHTML());
        populate(html);
        return html;
    }
}
