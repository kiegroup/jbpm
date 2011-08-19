package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TableLayoutFormItem;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.common.panels.ConfirmDialog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.PopupPanel;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class RemoveRowFormEffect extends FBFormEffect {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public RemoveRowFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().RemoveRowEffectLabel(), true);
    }
    
    @Override
    protected void createStyles() {
        final Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedY", getParent().getAbsoluteTop());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            @Override
            @SuppressWarnings("unchecked")
            public void undoAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedY = (Integer) event.getData("selectedY");
                List<FBFormItem> deletedRow = (List<FBFormItem>) event.getData("deletedRow");
                int rowNumber = item.getRowForYCoordinate(selectedY);
                item.addRow(rowNumber);
                item.insertRowElements(rowNumber, deletedRow);
            }
            @Override
            public void onEvent(UndoableEvent event) { }
            @Override
            public void doAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedY = (Integer) event.getData("selectedY");
                int rowNumber = item.getRowForYCoordinate(selectedY);
                List<FBFormItem> deletedRow = item.removeRow(rowNumber);
                event.setData("deletedRow", deletedRow);
            }
        }));
    }

    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof TableLayoutFormItem;
    }

    @Override
    public PopupPanel createPanel() {
        ConfirmDialog dialog = new ConfirmDialog(i18n.RemoveRowWarning());
        dialog.addOkButtonHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createStyles();
            }
        });
        return dialog;
    }
}
