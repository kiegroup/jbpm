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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jbpm.process.workitem.core.util.WorkItemHeaderInfo.Builder;
import org.kie.api.runtime.process.WorkItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemHeaderUtils {

    private WorkItemHeaderUtils() {}

    private static final String CONTENT_PREFIX = "HEADER_";
    private static final String PARAM_PREFIX = "HEADER_PARAM_";
    static final String SEPARATOR_PROP = "org.kie.workitem.ws.header.separator";

    private static final Logger logger = LoggerFactory.getLogger(WorkItemHeaderUtils.class);

    public static Collection<WorkItemHeaderInfo> getHeaderInfo(WorkItem workItem) {
        final String separator = System.getProperty(SEPARATOR_PROP, "_");
        Map<String, WorkItemHeaderInfo.Builder> map = new HashMap<>();
        for (Entry<String, Object> param : workItem.getParameters().entrySet()) {
            String key = param.getKey().toUpperCase();
            if (key.startsWith(PARAM_PREFIX)) {
                key = param.getKey().substring(PARAM_PREFIX.length());
                int indexOf = key.indexOf(separator);
                if (indexOf != -1) {
                    map.computeIfAbsent(key.substring(indexOf + separator.length()), Builder::of)
                       .withParam(key.substring(0, indexOf), param.getValue());
                } else {
                    logger.warn("Wrong parameter name {}. It expects at least one {} in {}", param.getKey(), separator,
                                key);
                }
            } else if (key.startsWith(CONTENT_PREFIX)) {
                map.computeIfAbsent(param.getKey().substring(CONTENT_PREFIX.length()), Builder::of)
                   .withContent(param.getValue());
            }
        }
        return map.values().stream().map(Builder::build).collect(Collectors.toList());
    }

}
