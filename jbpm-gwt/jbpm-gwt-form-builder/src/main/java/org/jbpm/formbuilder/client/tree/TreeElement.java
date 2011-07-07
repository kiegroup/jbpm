package org.jbpm.formbuilder.client.tree;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class TreeElement extends FocusPanel {

    private final FBFormItem item;
    private final Image img;
    private final Label itemName;
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final HorizontalPanel panel = new HorizontalPanel();
    
    public TreeElement(FBFormItem formItem) {
        panel.setSpacing(0);
        panel.setBorderWidth(0);
        this.item = formItem;
        if (formItem == null) {
            this.itemName = new Label("form");
            this.img = new Image(FormBuilderResources.INSTANCE.treeFolder());
        } else {
            this.itemName = new Label(formItem.getRepresentation().getTypeId());
            if (formItem instanceof FBCompositeItem) {
                this.img = new Image(FormBuilderResources.INSTANCE.treeFolder());
            } else {
                this.img = new Image(FormBuilderResources.INSTANCE.treeLeaf());
            }
        }
        panel.add(this.img);
        panel.add(this.itemName);
        addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                bus.fireEvent(new FormItemSelectionEvent(item, true));
            }
        });
        add(panel);
    }
    
    public boolean represents(FBFormItem item) {
        return this.item != null && this.item.equals(item);
    }
    
    public boolean represents(FBCompositeItem item) {
        return this.item != null && this.item.equals(item);
    }
}
