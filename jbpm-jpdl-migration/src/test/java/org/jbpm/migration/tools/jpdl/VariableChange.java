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
package org.jbpm.migration.tools.jpdl;

/**
 * Keeps change made to process variable.
 */
public class VariableChange {
    private String name;
    private Object oldValue;
    private Object newValue;

    public VariableChange() {
    }

    public VariableChange(String name, Object original) {
        this.name = name;
        this.oldValue = original;
    }

    public VariableChange(String name, Object original, Object changed) {
        this.name = name;
        this.oldValue = original;
        this.newValue = changed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariableChange other = (VariableChange) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.oldValue != other.oldValue && (this.oldValue == null || !this.oldValue.equals(other.oldValue))) {
            return false;
        }
        if (this.newValue != other.newValue && (this.newValue == null || !this.newValue.equals(other.newValue))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.oldValue != null ? this.oldValue.hashCode() : 0);
        hash = 29 * hash + (this.newValue != null ? this.newValue.hashCode() : 0);
        return hash;
    }

}
