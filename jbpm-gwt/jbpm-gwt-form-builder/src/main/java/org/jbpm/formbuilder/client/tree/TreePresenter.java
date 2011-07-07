package org.jbpm.formbuilder.client.tree;

import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedHandler;
import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class TreePresenter {

    private final TreeView view;
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public TreePresenter(TreeView treeView) {
        this.view = treeView;
        bus.addHandler(FormItemRemovedEvent.TYPE, new FormItemRemovedHandler() {
            public void onEvent(FormItemRemovedEvent event) {
                FBFormItem item = event.getFormItem();
                view.removeFormItem(item);
            }
        });
        bus.addHandler(FormItemAddedEvent.TYPE, new FormItemAddedHandler() {
            public void onEvent(FormItemAddedEvent event) {
                FBFormItem item = event.getFormItem();
                Widget parent = event.getFormItemHolder();
                FBCompositeItem parentItem = null;
                while (parent != null && parentItem == null) {
                    if (parent instanceof FBCompositeItem) {
                        parentItem = (FBCompositeItem) parent;
                    } else {
                        parent = parent.getParent();
                    }
                }
                view.addFormItem(item, parentItem);
            }
        });
    }

}
