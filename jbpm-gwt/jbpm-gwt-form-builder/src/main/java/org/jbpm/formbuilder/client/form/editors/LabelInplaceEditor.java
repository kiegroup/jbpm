package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.items.LabelFormItem;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class LabelInplaceEditor extends FBInplaceEditor {

    private final TextBox textBox = new TextBox();
    private final FocusWrapper wrapper = new FocusWrapper();
    
    public LabelInplaceEditor(final LabelFormItem item) {
        final HorizontalPanel editPanel = new HorizontalPanel();
        editPanel.setBorderWidth(1);
        textBox.setValue(item.getLabel().getText());
        textBox.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                wrapper.setValue(false);
                item.reset();
            }
        });
        textBox.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                wrapper.setValue(true);
            }
        });
        textBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                item.getLabel().setText(textBox.getValue());
                item.reset();
            }
        });
        editPanel.add(textBox);
        textBox.setWidth(item.getLabel().getOffsetWidth() + "px");
        textBox.setFocus(true);
        add(editPanel);
    }
    
    @Override
    public void focus() {
        textBox.setFocus(true);
    }
    
    @Override
    public boolean isFocused() {
        return wrapper.getValue();
    }

}
