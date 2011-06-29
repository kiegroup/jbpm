package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class CheckBoxRepresentation extends FormItemRepresentation {

    private String formValue;
    private Boolean checked;
    private String name;
    private String id;

    public CheckBoxRepresentation() {
        super("checkBox");
    }
    
    public String getFormValue() {
        return formValue;
    }

    public void setFormValue(String formValue) {
        this.formValue = formValue;
    }

    public Boolean getChecked() {
        return checked == null ? Boolean.FALSE : checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = super.getData();
        data.put("formValue", this.formValue);
        data.put("checked", this.checked);
        data.put("name", this.name);
        data.put("id", this.id);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        super.setData(data);
        this.formValue = (String) data.get("formValue");
        this.checked = (Boolean) data.get("checked");
        this.name = (String) data.get("name");
        this.id = (String) data.get("id");
    }
}
