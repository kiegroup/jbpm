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

package org.jbpm.process.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public interface AuditLoggerArchiveTreat {

    default List<ArchiveLoggerProvider> initArchiveLoggerProvider() {
        List<String> props = Arrays.asList(System.getProperty("org.kie.jbpm.persistence.archive-provider", "").split(","));
        List<ArchiveLoggerProvider> archiveLoggerProvider = new ArrayList<>();
        Iterator<ArchiveLoggerProvider> iterator = ServiceLoader.load(ArchiveLoggerProvider.class).iterator();
        while (iterator.hasNext()) {
            ArchiveLoggerProvider provider = iterator.next();
            if (props.contains(provider.getClass().getCanonicalName())) {
                archiveLoggerProvider.add(provider);
            }
        }
        return archiveLoggerProvider;
    }

}
