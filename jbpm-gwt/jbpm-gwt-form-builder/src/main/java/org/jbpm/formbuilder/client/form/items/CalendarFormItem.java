/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.items.CalendarRepresentation;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class CalendarFormItem extends FBFormItem {

    private String defaultValue;
    private final DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
    private final DatePicker calendar = new DatePicker();
    private final Image icon;
    private final PopupPanel panel = new PopupPanel();
    private final TextBox text = new TextBox();
    
    public CalendarFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public CalendarFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        icon = new Image(FormBuilderResources.INSTANCE.calendarSquare());
        icon.getElement().getStyle().setCursor(Cursor.POINTER);
        icon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.setSize("183px", "183px");
                panel.setPopupPosition(event.getClientX(), event.getClientY());
                panel.setWidget(calendar);
                panel.show();
                
            }
        });
        calendar.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                panel.hide();
                text.setValue(format.format(event.getValue()));
            }
        });
        calendar.setSize("183px", "183px");
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(text);
        hPanel.add(icon);
        text.setSize("175px", "21px");
        hPanel.setSize("200px", "21px");
        setSize("200px", "21px");
        add(hPanel);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("width", getWidth());
        map.put("height", getHeight());
        map.put("defaultValue", this.defaultValue); //TODO add the rest of the properties (icon url, icon update, style for the calendar, etc)
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        setWidth(extractString(asPropertiesMap.get("width")));
        setHeight(extractString(asPropertiesMap.get("height")));
        this.defaultValue = extractString(asPropertiesMap.get("defaultValue"));
        populate(this.calendar);
    }
    
    private void populate(DatePicker calendar) {
        if (getHeight() != null && !"".equals(getHeight())) {
            calendar.setHeight(getHeight());
        }
        if (getWidth() != null && !"".equals(getWidth())) {
            calendar.setWidth(getWidth());
        }
        if (this.defaultValue != null) {
            if (!"".equals(this.defaultValue)) {
                calendar.setValue(format.parse(this.defaultValue));
            } else {
                calendar.setValue(null);
            }

        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        return super.getRepresentation(new CalendarRepresentation());
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof CalendarRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "CalendarRepresentation"));
        }
        super.populate(rep);
        CalendarRepresentation crep = (CalendarRepresentation) rep;
        if (crep.getWidth() != null && !"".equals(crep.getWidth())) {
            setWidth(crep.getWidth());
        }
        if (crep.getHeight() != null && !"".equals(crep.getHeight())) {
            setHeight(crep.getHeight());
        }
        populate(this.calendar);
    }
    
    @Override
    public FBFormItem cloneItem() {
        CalendarFormItem clone = super.cloneItem(new CalendarFormItem());
        populate(clone.calendar);
        return clone;
    }

    @Override
    public Widget cloneDisplay() {
        DatePicker display = new DatePicker();
        populate(display);
        return display;
    }

}
