/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.bpmn2.handler;

import org.jbpm.process.core.event.EventTransformerImpl;
import org.jbpm.workflow.core.node.Transformation;
import org.kie.api.runtime.process.ProcessContext;

class VariableResolver {

    private VariableResolver() {}

    public static Object getVariable(ProcessContext kcontext, String varName) {
        Object tVariable = null;
        if (varName != null) {
            tVariable = kcontext.getVariable(varName);
            if (tVariable == null) {
                tVariable = varName;
            }
            Transformation transformation = (Transformation) kcontext.getNodeInstance().getNode()
                    .getMetaData().get("Transformation");
            if (transformation != null) {
                tVariable = new EventTransformerImpl(transformation).transformEvent(tVariable);
            }
        }
        return tVariable;
    }
}
