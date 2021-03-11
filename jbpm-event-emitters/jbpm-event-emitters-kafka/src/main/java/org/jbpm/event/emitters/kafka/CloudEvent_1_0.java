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
package org.jbpm.event.emitters.kafka;

import java.util.Date;
import java.util.UUID;

class CloudEventSpec1 {

    private String specversion = "1.0";
    private Date time = new Date();
    private String id = UUID.randomUUID().toString();
    private String type;
    private String source;
    private Object data;

    public CloudEventSpec1(String type, String source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public String getSpecversion() {
        return specversion;
    }

    public Date getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }

}
