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

package org.jbpm.casemgmt.cmmn.xml.util;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.compiler.xml.ProcessBuildData;

public class CaseMetadataSetAtrribute<T> {
    
    private String name;
    private ProcessBuildData data;

    CaseMetadataSetAtrribute(ProcessBuildData data, String name) {
        this.name = name;
        this.data = data;
        if(this.data.getMetaData(this.name) == null) {
            this.data.setMetaData(this.name, new HashSet<>());
        }
    }

    public Set<T> get() {
        return (Set<T>) data.getMetaData(this.name);
    }

    public void add(T item) {
        get().add(item);
    }

    public void remove(T item) {
        get().remove(item);
    }
}
