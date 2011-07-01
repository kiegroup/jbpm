package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ConditionalBlockRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ConditionalBlockFormItem extends LayoutFormItem {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private VerticalPanel display = new VerticalPanel() {
        @Override
        public void add(Widget w) {
            if (w instanceof FBFormItem) {
                FBFormItem item = (FBFormItem) w;
                if (ifBlock == null) {
                    ifBlock = item;
                } else if (elseBlock == null) {
                    elseBlock = item;
                } else {
                    bus.fireEvent(new NotificationEvent(Level.WARN, 
                            "You shouldn't add more than two layout components to a conditional block (one for if and one for else)")
                    );
                }
            }
            super.add(w);
        }
        
        @Override
        public boolean remove(Widget w) {
            if (w == ifBlock) {
                ifBlock = null;
            }
            if (w == elseBlock) {
                elseBlock = null;
            }
            return super.remove(w);
        }
    };
    
    private FBFormItem ifBlock = null;
    private FBFormItem elseBlock = null;
    private String conditionScript = "true";
    
    public ConditionalBlockFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        display.setBorderWidth(1);
        display.setStyleName("conditionalBlockBorder");
        add(display);
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("conditionScript", this.conditionScript);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.conditionScript = extractString(asPropertiesMap.get("conditionScript"));
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        ConditionalBlockRepresentation rep = super.getRepresentation(new ConditionalBlockRepresentation());
        rep.setCondition(conditionScript);
        rep.setIfBlock(ifBlock == null ? null : ifBlock.getRepresentation());
        rep.setElseBlock(elseBlock == null ? null : elseBlock.getRepresentation());
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof ConditionalBlockRepresentation)) {
            throw new FormBuilderException("rep should be of type ConditionalBlockRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        ConditionalBlockRepresentation srep = (ConditionalBlockRepresentation) rep;
        this.conditionScript = srep.getCondition();
        FormItemRepresentation ifRep = srep.getIfBlock();
        if (ifRep == null) {
            this.ifBlock = null;
        } else {
            this.ifBlock = createItem(ifRep);
        }
        FormItemRepresentation elseRep = srep.getElseBlock();
        if (elseRep == null) {
            this.elseBlock = null;
        } else {
            this.elseBlock = createItem(elseRep);
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        ConditionalBlockFormItem item = new ConditionalBlockFormItem(super.getFormEffects());
        item.conditionScript = this.conditionScript;
        item.elseBlock = this.elseBlock == null ? null : this.elseBlock.cloneItem();
        item.ifBlock = this.ifBlock == null ? null : this.ifBlock.cloneItem();
        return item;
    }

    @Override
    public Widget cloneDisplay() {
        Panel clone = ((ConditionalBlockFormItem) cloneItem()).getPanel();
        return clone;
    }

    @Override
    public Panel getPanel() {
        return display;
    }

}
