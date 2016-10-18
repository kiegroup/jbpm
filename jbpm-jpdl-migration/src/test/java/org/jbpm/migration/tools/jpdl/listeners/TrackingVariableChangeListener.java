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
package org.jbpm.migration.tools.jpdl.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.migration.tools.jpdl.VariableChange;
import org.jbpm.context.exe.ContextInstance;

/**
 * Tracks variable changes inside action handlers.
 * 
 */
public class TrackingVariableChangeListener implements VariableChangeListener {

    private Map<String, List<VariableChange>> changes = new HashMap<String, List<VariableChange>>();

    /**
     * Records the state of variables in given context instance.
     *
     * @param cxt
     *            context instance containing variables.
     *
     *            Must be called before making changes to variables in order to
     *            record the original state of variables.
     */
    @Override
    public synchronized void recordOldValues(ContextInstance cxt) {
        Map<String, Object> vars = cxt.getVariables();
        Object value;
        for (String varName : vars.keySet()) {
            value = vars.get(varName);
            VariableChange vch = new VariableChange(varName, value);
            List<VariableChange> varChanges = changes.get(varName);
            if (varChanges == null) {
                varChanges = new ArrayList<VariableChange>();
                changes.put(varName, varChanges);
            }
            varChanges.add(vch);
        }
    }

    /**
     * Records the state of variables in given context instance.
     *
     * @param cxt
     *            context instance containing variables.
     *
     *            Must be called after making changes to variables in order to
     *            record the new state of variables.
     */
    @Override
    public synchronized void recordNewValues(ContextInstance cxt) {
        Map<String, Object> vars = cxt.getVariables();
        for (String varName : vars.keySet()) {
            VariableChange vch = getLastChange(varName);
            if (vch == null || vch.getNewValue() != null) {
                throw new IllegalStateException(
                        "trying to record new value of variable, but the original value is missing");
            }
            vch.setNewValue(vars.get(varName));
        }
    }

    /**
     * Gets last recorded change made to given variable
     *
     * @param varName
     *            name of variable
     * @return info about last change made to <code>varName</code>
     * @throws RuntimeException
     *             when there is no change of this variable.
     */
    public VariableChange getLastChange(String varName) {
        List<VariableChange> varChanges = getChanges(varName);
        if (varChanges.isEmpty()) {
            throw new RuntimeException(String.format("no change has been made to variable %s!", varName));
        } else {
            return varChanges.get(varChanges.size() - 1);
        }
    }

    /**
     * Gets all recorded changes made to given variable
     *
     * @param varName
     *            name of variable
     * @return list of all changes made to <code>varName</code>
     */
    public List<VariableChange> getChanges(String varName) {
        List<VariableChange> varChanges = changes.get(varName);
        return varChanges == null ? new ArrayList<VariableChange>() : varChanges;
    }
}
