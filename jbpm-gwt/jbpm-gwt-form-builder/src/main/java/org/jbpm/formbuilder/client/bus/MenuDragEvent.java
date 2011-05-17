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
package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class MenuDragEvent extends GwtEvent<MenuDragEventHandler> {

    public static Type<MenuDragEventHandler> TYPE = new Type<MenuDragEventHandler>();
    
    private final String itemId;
    private final int x;
    private final int y;
    
    public MenuDragEvent(String itemId, int x, int y) {
        super();
        this.itemId = itemId;
        this.x = x;
        this.y = y;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }

    @Override
    public Type<MenuDragEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuDragEventHandler handler) {
        handler.onEvent(this);
        
    }

}
