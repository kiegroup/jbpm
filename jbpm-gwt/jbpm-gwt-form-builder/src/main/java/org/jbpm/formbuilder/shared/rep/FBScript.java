package org.jbpm.formbuilder.shared.rep;

public class FBScript {

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

    public String getJsonCode() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("'documentation': ").append(jsonString(documentation)).append(", ");
        builder.append("'id': ").append(jsonString(id)).append(", ");
        builder.append("'type': ").append(jsonString(type)).append(", ");
        builder.append("'src': ").append(jsonString(src)).append(", ");
        builder.append("'content': ").append(jsonString(content)).append(", ");
        builder.append("'invokeFunction': ").append(jsonString(invokeFunction)).append(", ");
        return builder.append("}").toString();
    }
    
    private String jsonString(String value) {
        if (value == null) {
            return "null";
        } else {
            return "'" + value + "'";
        }
    }
}
