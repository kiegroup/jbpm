package org.jbpm.formbuilder.client.bus;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBScript;

import com.google.gwt.event.shared.GwtEvent;

public class EvaluateScriptEvent extends GwtEvent<EvaluateScriptEventHandler> {

    public static final Type<EvaluateScriptEventHandler> TYPE = new Type<EvaluateScriptEventHandler>();
    
    private Map<String, Object> input = new HashMap<String, Object>();
    private FBScript script;
    
    public EvaluateScriptEvent(FBScript script) {
        this.script = script;
    }
    
    public EvaluateScriptEvent(FBScript script, Map<String, Object> input) {
        this.script = script;
        this.input = input;
    }
    
    public Map<String, Object> getInput() {
        return input;
    }
    
    public FBScript getScript() {
        return script;
    }
    
    public Object getInput(String key) {
        return input.get(key);
    }
    
    public Object putInput(String key, Object value) {
        return input.put(key, value);
    }
    
    @Override
    public Type<EvaluateScriptEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EvaluateScriptEventHandler handler) {
        handler.onEvent(this);
    }

}
