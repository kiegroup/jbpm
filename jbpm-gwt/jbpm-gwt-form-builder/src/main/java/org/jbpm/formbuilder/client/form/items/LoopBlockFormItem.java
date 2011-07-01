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
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.items.LoopBlockRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class LoopBlockFormItem extends LayoutFormItem {

    private String variableName;
    private SimplePanel loopBlock = new SimplePanel();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public LoopBlockFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        loopBlock.setStyleName("loopBlockBorder");
        add(loopBlock);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("variableName", this.variableName);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.variableName = extractString(asPropertiesMap.get("variableName"));
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        LoopBlockRepresentation rep = getRepresentation(new LoopBlockRepresentation());
        rep.setInputName(getInput() == null ? null : getInput().getName());
        FBFormItem loopItem = (FBFormItem) this.loopBlock.getWidget();
        if (loopItem != null) {
            rep.setLoopBlock(loopItem.getRepresentation());
        }
        rep.setVariableName(this.variableName);
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof LoopBlockRepresentation)) {
            throw new FormBuilderException("rep should be of type LoopBlockRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        LoopBlockRepresentation lrep = (LoopBlockRepresentation) rep;
        this.variableName = lrep.getVariableName();
        this.loopBlock.clear();
        if (lrep.getInputName() != null && !"".equals(lrep.getInputName())) {
            InputData input = new InputData();
            input.setName(lrep.getInputName());
            lrep.setInput(input);
        }
        if (lrep.getLoopBlock() != null) {
            FBFormItem child = super.createItem(lrep.getLoopBlock());
            this.loopBlock.add(child);
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        LoopBlockFormItem clone = super.cloneItem(new LoopBlockFormItem(getFormEffects()));
        FBFormItem loopItem = (FBFormItem) this.loopBlock.getWidget();
        clone.add(loopItem);
        clone.variableName = this.variableName;
        return clone;
    }

    @Override
    public Widget cloneDisplay() {
        SimplePanel display = new SimplePanel();
        display.setStyleName("loopBlockBorder");
        display.setWidget(loopBlock.getWidget());
        display.setSize(getWidth(), getHeight());
        return display;
    }

    @Override
    public Panel getPanel() {
        return loopBlock;
    }

    @Override
    public boolean add(FBFormItem item) {
        if (loopBlock.getWidget() == null) {
            loopBlock.setWidget(item);
        } else {
            bus.fireEvent(new NotificationEvent(Level.WARN, "Loop block can only contain one item"));
        }
        return super.add(item);
    }
}
