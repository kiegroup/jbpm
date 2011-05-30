package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalAlignmentFormEffect extends FBFormEffect {

    private ListBox alignmentBox = new ListBox();
    
    public HorizontalAlignmentFormEffect() {
        super(createImage(), true);
    }
    
    private static Image createImage() {
        Image image = new Image(FormBuilderResources.INSTANCE.alignmentEffect());
        image.setAltText("alignment");
        return image;
    }
    
    @Override
    protected void createStyles() {
        int index = this.alignmentBox.getSelectedIndex();
        String value = this.alignmentBox.getValue(index);
        Widget widget = getWidget();
        if (value == "left") {
            if (widget instanceof HasHorizontalAlignment) {
                HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
                hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            }
        }
        if (value == "right") {
            if (widget instanceof HasHorizontalAlignment) {
                HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
                hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            }
        }
        if (value == "center") {
            if (widget instanceof HasHorizontalAlignment) {
                HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
                hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            }
        }
        if (value == "justify") {
            if (widget instanceof HasHorizontalAlignment) {
                HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
                hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
            }
        }
    }
    
    @Override
    public void remove(FBFormItem item) {
        super.remove(item);
        Widget widget = getWidget();
        if (widget instanceof HasHorizontalAlignment) {
            HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
            hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
        }
    }
    
    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        panel.setSize("300px", "200px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(new Label("Alignment:"));
        alignmentBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                HorizontalAlignmentFormEffect.this.createStyles();
                panel.hide();
            };
        });
        hPanel.add(alignmentBox);
        Button fontSizeButton = new Button("Apply");
        fontSizeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                HorizontalAlignmentFormEffect.this.createStyles();
                panel.hide();
            }
        });
        vPanel.add(alignmentBox);
        vPanel.add(fontSizeButton);
        panel.add(vPanel);
        return panel;
    }
}
