package org.jbpm.formbuilder.shared.rep;

import java.util.HashMap;
import java.util.Map;

public class FBScript implements Mappable {

    private String documentation;
    private String id;
    
    private String type;
    private String src;
    private String content;
    private String invokeFunction;

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInvokeFunction() {
        return invokeFunction;
    }

    public void setInvokeFunction(String invokeFunction) {
        this.invokeFunction = invokeFunction;
    }

    public Map<String, Object> getDataMap() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("documentation", this.documentation);
        data.put("id", this.id);
        data.put("type", this.type);
        data.put("src", this.src);
        data.put("content", this.content);
        data.put("invokeFunction", this.invokeFunction);
        return data;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        this.documentation = (String) dataMap.get("documentation");
        this.id = (String) dataMap.get("id");
        this.type = (String) dataMap.get("type");
        this.src = (String) dataMap.get("src");
        this.content = (String) dataMap.get("content");
        this.invokeFunction = (String) dataMap.get("invokeFunction");
    }
}
