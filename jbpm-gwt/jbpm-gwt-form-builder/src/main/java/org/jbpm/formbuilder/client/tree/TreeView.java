package org.jbpm.formbuilder.client.tree;

import java.util.List;

import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class TreeView extends ScrollPanel {

    private Tree tree = new Tree();
    
    public TreeView() {
        tree.setSize("100%", "100%");
        tree.addItem(new TreeElement(null));
        setWidget(tree);
    }
    
    public void addFormItem(FBFormItem item, FBCompositeItem parent) {
        TreeItem treeBranch = tree.getItem(0);
        if (parent != null && parent instanceof FBFormItem) {
            TreeItem treeBranch2 = getItem(treeBranch, (FBFormItem) parent);
            if (treeBranch2 != null) {
                treeBranch = treeBranch2;
            }
        }
        treeBranch.addItem(new TreeElement(item));
        if (item instanceof FBCompositeItem) {
            FBCompositeItem compItem = (FBCompositeItem) item;
            List<FBFormItem> subItems = compItem.getItems();
            if (subItems != null) {
                for (FBFormItem subItem : subItems) {
                    if (subItem != null) {
                        addFormItem(subItem, compItem);
                    }
                }
            }
        }
    }
    
    public void removeFormItem(FBFormItem item) {
        TreeItem treeBranch = tree.getItem(0);
        TreeItem treeBranch2 = getItem(treeBranch, item);
        if (treeBranch2 != null) {
            treeBranch = treeBranch2;
        }
        treeBranch.getParentItem().removeItem(treeBranch);
    }

    private TreeItem getItem(TreeItem treeBranch, FBFormItem item) {
        TreeItem retval = null;
        for (int index = 0; retval == null && index < treeBranch.getChildCount(); index++) {
            TreeItem treeItem = treeBranch.getChild(index);
            TreeElement elem = (TreeElement) treeItem.getWidget();
            if (elem.represents(item)) {
                retval = treeItem;
            } else {
                retval = getItem(treeItem, item);
            }
        }
        return retval;
    }
}
