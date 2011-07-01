package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.FileInputRepresentation;

import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Widget;

public class FileInputFormItem extends FBFormItem {

    private FileUpload fileUpload = new FileUpload();
    
    private String name;
    private String id;
    private String accept;
    
    public FileInputFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(fileUpload);
        setHeight("15px");
        setWidth("100px");
        fileUpload.setSize(getWidth(), getHeight());
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("width", getWidth());
        map.put("height", getHeight());
        map.put("accept", this.accept);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.name = extractString(asPropertiesMap.get("name"));
        setWidth(extractString(asPropertiesMap.get("width")));
        setHeight(extractString(asPropertiesMap.get("height")));
        this.id = extractString(asPropertiesMap.get("id"));
        this.accept = extractString(asPropertiesMap.get("accept"));

        populate(this.fileUpload);
    }

    private void populate(FileUpload fileUpload) {
        if (this.name != null) {
            fileUpload.setName(this.name);
        }
        if (getWidth() != null) {
            fileUpload.setWidth(getWidth());
        }
        if (getHeight() != null) {
            fileUpload.setHeight(getHeight());
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        FileInputRepresentation rep = super.getRepresentation(new FileInputRepresentation());
        rep.setId(this.id);
        rep.setName(this.name);
        rep.setAccept(this.accept);
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof FileInputRepresentation)) {
            throw new FormBuilderException("rep should be of type FileInputRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        FileInputRepresentation frep = (FileInputRepresentation) rep;
        this.id = frep.getId();
        this.name = frep.getName();
        this.accept = frep.getAccept();
        populate(this.fileUpload);
    }
    
    @Override
    public FBFormItem cloneItem() {
        FileInputFormItem clone = new FileInputFormItem(getFormEffects());
        clone.accept = this.accept;
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.name = this.name;
        clone.setWidth(this.getWidth());
        clone.populate(clone.fileUpload);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        FileUpload fu = new FileUpload();
        populate(fu);
        return fu;
    }
}
