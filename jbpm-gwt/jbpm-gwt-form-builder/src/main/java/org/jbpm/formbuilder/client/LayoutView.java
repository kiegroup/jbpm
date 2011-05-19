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
package org.jbpm.formbuilder.client;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public class LayoutView extends SimplePanel {

    AbsolutePanel layout = new AbsolutePanel();
    
    public LayoutView() {
        setSize("500px", "500px");
        Grid grid = new Grid(1,1);
        grid.setSize("100%", "100%");
        grid.setBorderWidth(2);
        grid.setWidget(0, 0, layout);
        add(grid);
    }

    public Panel getUnderlyingLayout(int x, int y) {
        return layout; // TODO Implement a way to see what's in that position
    }
}
