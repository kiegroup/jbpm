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
package org.jbpm.migration.tools.bpmn2;

/**
 * Information about class.method execution.
 */
public class ExecutionInfo {
    private String className;
    private String methodName;
    private int count;

    public ExecutionInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.count = 1;
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    /**
     * resets the execution count to 0.
     *
     * @return actual instance
     */
    public ExecutionInfo reset() {
        count = 0;
        return this;
    }

    /**
     * increases the execution count by 1.
     *
     * @return actual instance
     */
    public ExecutionInfo inc() {
        count++;
        return this;
    }

    public int ExecutionCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExecutionInfo other = (ExecutionInfo) obj;
        if ((this.className == null) ? (other.className != null) : !this.className.equals(other.className)) {
            return false;
        }
        if ((this.methodName == null) ? (other.methodName != null) : !this.methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 89 * hash + (this.methodName != null ? this.methodName.hashCode() : 0);
        return hash;
    }

}
