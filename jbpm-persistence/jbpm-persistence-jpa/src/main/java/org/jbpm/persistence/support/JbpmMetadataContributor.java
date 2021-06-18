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

package org.jbpm.persistence.support;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MetadataContributor;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.jboss.jandex.IndexView;
import org.jbpm.persistence.api.JbpmEntityContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmMetadataContributor implements MetadataContributor {

    private static final Logger log = LoggerFactory.getLogger(JbpmMetadataContributor.class);

    private List<JbpmEntityContributor> contributors;

    public JbpmMetadataContributor() {
        contributors = new ArrayList<>();
        ServiceLoader<JbpmEntityContributor> serviceLoader = ServiceLoader.load(JbpmEntityContributor.class);
        serviceLoader.forEach(sl -> contributors.add(sl));
    }

    @Override
    public void contribute(InFlightMetadataCollector metadataCollector, IndexView jandexIndex) {
        List<String> entityDisableChecks = contributors.stream().flatMap(e -> e.disableInsertChecks().stream()).collect(Collectors.toList());
        for(String entity : entityDisableChecks) {
            log.debug("disabling row count check for entity {}", entity);
            metadataCollector.getEntityBindingMap().get(entity).setCustomSQLInsert(null, false, ExecuteUpdateResultCheckStyle.NONE);
        }
    }

}
