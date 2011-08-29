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
package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedHandler;
import org.jbpm.formbuilder.client.effect.view.IoBindingEffectView;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.api.Formatter;
import org.jbpm.formbuilder.shared.api.InputData;
import org.jbpm.formbuilder.shared.api.OutputData;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.PopupPanel;
import com.gwtent.reflection.client.Reflectable;

/**
 * Allows to bind a task input or output to a given UI component
 */
@Reflectable
public class IoBindingEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private TaskRef ioRef = null;
    private TaskPropertyRef input = null;
    private TaskPropertyRef output = null;
    
    public IoBindingEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().IoBindingEffectLabel(), true);
        bus.addHandler(TaskSelectedEvent.TYPE, new TaskSelectedHandler() {
            @Override
            public void onSelectedTask(TaskSelectedEvent event) {
                if (event.getSelectedTask() != null) {
                    ioRef = event.getSelectedTask();
                }
            }
        });
    }

    public void fire() {
        createStyles();
    }
    
    @Override
    protected void createStyles() {
        FBFormItem item = getItem();
        InputData in = null;
        if (this.input != null) {
            in = new InputData();
            in.setName(this.input.getName());
            in.setValue(this.input.getSourceExpresion());
            in.setMimeType("multipart/form-data");
            in.setFormatter(new Formatter() {
                @Override
                public Object format(Object object) {
                    return object;
                }
                @Override
                public Map<String, Object> getDataMap() {
                	return new HashMap<String, Object>();
                }
            });
        }
        item.setInput(in);
        OutputData out = null;
        if (this.output != null) {
            out = new OutputData();
            out.setName(this.output.getName());
            out.setValue(this.output.getSourceExpresion());
            out.setMimeType("multipart/form-data");
            out.setFormatter(new Formatter() {
                @Override
                public Object format(Object object) {
                    return object;
                }
                @Override
                public Map<String, Object> getDataMap() {
                	return new HashMap<String, Object>();
                }
            });
        }
        item.setOutput(out);
    }

    @Override
    public FBFormItem getItem() {
        return super.getItem();
    }
    
    public TaskRef getIoRef() {
        return ioRef;
    }

    public TaskPropertyRef getInput() {
        return input;
    }

    public void setInput(TaskPropertyRef input) {
        this.input = input;
    }

    public TaskPropertyRef getOutput() {
        return output;
    }

    public void setOutput(TaskPropertyRef output) {
        this.output = output;
    }

    @Override
    public PopupPanel createPanel() {
        return new IoBindingEffectView(this);
    }
    
    @Override
    public boolean isValidForItem(FBFormItem item) {
        return this.ioRef != null;
    }
}
