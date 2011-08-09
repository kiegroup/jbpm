/**
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
package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to represent future position of a droppable item.
 */
public class PhantomPanel extends SimplePanel {

    /**
     * Label for IE quirks mode workaround.
     */
    private static final Label DUMMY_LABEL = new HTML("&nbsp;");
    
    public PhantomPanel() {
        setStyleName("phantomPanel");
        setWidget(DUMMY_LABEL);
        setSize("100%", "5px");
    }
    
    public void selfInsert(Panel panel, int x, int y, List<FBFormItem> items) {
        boolean placed = false;
        for (FBFormItem item : items) {
            int left = item.getAbsoluteLeft();
            int right = left + item.getOffsetWidth();
            int top = item.getAbsoluteTop();
            int bottom = top + item.getOffsetHeight();
            if (x > left && x > right && y > top && y < bottom) {
                //inside this panel
                insert(panel, item);
                placed = true;
                break;
            }
            //TODO see how to place yourself in correct position of the panel
        }
        if (!placed) {
            int right = panel.getAbsoluteLeft() + panel.getOffsetWidth();
            int bottom = panel.getAbsoluteTop() + panel.getOffsetHeight();
            if (x > right || y > bottom) {
                panel.add(this);
            }
        }
    }
    
    protected void insert(Panel panel, Widget beforeWidget) {
        Iterator<Widget> iter = panel.iterator();
        while (iter.hasNext()) {
            Widget widget = iter.next();
            if (widget == beforeWidget) { //supposed to be the same object
                List<Widget> nextWidgets = new ArrayList<Widget>();
                iter.remove();
                nextWidgets.add(widget);
                while (iter.hasNext()) {
                    nextWidgets.add(iter.next());
                    iter.remove();
                }
                panel.add(this);
                for (Widget movedWidget : nextWidgets) {
                    panel.add(movedWidget);
                }
                return;
            }
        }
    }
    
    
    public static int selfClear(Panel panel) {
        int index = 0;
        Widget childToRemove = null;
        for (Widget child : panel) {
            if (child instanceof PhantomPanel) {
                childToRemove = child;
                break;
            }
            index++;
        }
        if (childToRemove == null) {
            index = -1;
        } else {
            panel.remove(childToRemove);
        }
        return index;
    }
}
