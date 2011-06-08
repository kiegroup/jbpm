package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class HorizontalPanelRepresentation extends FormItemRepresentation {

    private Integer borderWidth;
    private Integer spacing;
    private String cssClassName;
    private String horizontalAlignment;
    private String verticalAlignment;
    private String title;
    private String id;
    private List<FormItemRepresentation> items = new ArrayList<FormItemRepresentation>();
    
    public HorizontalPanelRepresentation() {
        super("horizontalPanel");
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Integer getSpacing() {
        return spacing;
    }

    public void setSpacing(Integer spacing) {
        this.spacing = spacing;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public List<FormItemRepresentation> getItems() {
        return items;
    }
    
    public void addItem(FormItemRepresentation item) {
        items.add(item);
    }
    
    public void setItems(List<FormItemRepresentation> items) {
        this.items = items;
    }
}
