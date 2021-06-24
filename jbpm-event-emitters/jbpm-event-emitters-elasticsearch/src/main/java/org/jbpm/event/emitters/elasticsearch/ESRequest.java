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
package org.jbpm.event.emitters.elasticsearch;

public class ESRequest {

    private String index;
    private String id;
    private String operation;
    private String type;
    private Object body;
    
   

    public ESRequest(String index, String id, String type, String operation, Object body) {
        this.index = index;
        this.id = id;
        this.operation = operation;
        this.type = type;
        this.body = body;
    }

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getType() {
        return type;
    }

    public Object getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ESRequest [index=" + index + ", id=" + id + ", operation=" + operation + ", type=" + type + ", body=" +
               body + "]";
    }

}
