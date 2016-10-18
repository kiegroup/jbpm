/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.migration.tools.jpdl.handlers;

import java.util.Map;

import org.jbpm.migration.tools.jpdl.listeners.VariableChangeListener;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * TODO: add mechanism to specify which and how variables should be changed. =>
 * List of variable names, old and new values.
 */
public class VariableActionHandler implements ActionHandler {

    private static VariableChangeListener variableListener;
    private static Map<String, Object> varChanges;

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        ContextInstance ci = executionContext.getContextInstance();

        variableListener.recordOldValues(ci);

        makeChanges(varChanges, executionContext);

        variableListener.recordNewValues(ci);

        executionContext.getProcessInstance().signal(); // complete the node
                                                        // inside the subprocess
    }

    private void makeChanges(Map<String, Object> changes, ExecutionContext executionContext) {
        ContextInstance ci = executionContext.getContextInstance();
        ci.setVariables(changes);
    }

    public static void setVariableListener(VariableChangeListener listener) {
        variableListener = listener;
    }

    public static void setVariableChanges(Map<String, Object> changes) {
        varChanges = changes;
    }
}
