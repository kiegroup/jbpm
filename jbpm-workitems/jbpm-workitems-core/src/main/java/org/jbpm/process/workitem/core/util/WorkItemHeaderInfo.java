/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.core.util;

import java.util.HashMap;
import java.util.Map;

public class WorkItemHeaderInfo {

    private final String name;
    private final Object content;
    private final Map<String, Object> params;

    public static class Builder {

        private final String name;
        private Object content;
        private Map<String, Object> parameters;

        private Builder(String name) {
            this.name = name;
            this.parameters = new HashMap<>();
        }

        public static Builder of(String name) {
            return new Builder(name);
        }

        public Builder withContent(Object content) {
            this.content = content;
            return this;
        }

        public Builder withParam(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        public WorkItemHeaderInfo build() {
            return new WorkItemHeaderInfo(name, content, parameters);
        }
    }

    private WorkItemHeaderInfo(String name, Object content, Map<String, Object> params) {
        this.name = name;
        this.content = content;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public Object getContent() {
        return content;
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    @Override
    public String toString() {
        return "WorkItemHeaderInfo [name=" + name + ", content=" + content + ", params=" + params + "]";
    }
}
