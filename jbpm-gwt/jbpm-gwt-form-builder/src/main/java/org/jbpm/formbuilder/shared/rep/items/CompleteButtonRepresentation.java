package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class CompleteButtonRepresentation extends FormItemRepresentation {

    private String text;
    private String name;
    private String id;
    private FBScript onClickScript;
    
    public CompleteButtonRepresentation() {
        super("completeButton");
        this.onClickScript = new FBScript();
        this.onClickScript.setType("text/javascript");
        this.onClickScript.setContent("document.forms[0].submit();");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public FBScript getOnClickScript() {
        return onClickScript;
    }

    public void setOnClickScript(FBScript onClickScript) {
        this.onClickScript = onClickScript;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        // TODO Auto-generated method stub
        
    }
}
