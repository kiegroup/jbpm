/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.persistence.scripts;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmDialectResolver implements DialectResolver {
 
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(JbpmDialectResolver.class);

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info){
        Dialect d;
        if ("Adaptive Server Enterprise".equals(info.getDatabaseName())) {
           return new SybaseJbpmDialect(); 
        } else {
            d = StandardDialectResolver.INSTANCE.resolveDialect(info);
        }
        logger.info("resolveDialect: {}", d);
        return d;
    }
}