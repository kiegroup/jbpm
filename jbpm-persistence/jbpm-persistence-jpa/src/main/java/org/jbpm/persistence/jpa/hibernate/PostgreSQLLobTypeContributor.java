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

package org.jbpm.persistence.jpa.hibernate;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.StandardBasicTypeTemplate;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;


public class PostgreSQLLobTypeContributor implements TypeContributor {

    public class ByteaContributorType extends StandardBasicTypeTemplate<byte[]> {

        private static final long serialVersionUID = 1619875355308645967L;

        public ByteaContributorType() {
            super(BinaryTypeDescriptor.INSTANCE, PrimitiveByteArrayTypeDescriptor.INSTANCE, StandardBasicTypes.MATERIALIZED_BLOB.getName());
        }

    }

    public class TextContributorType extends StandardBasicTypeTemplate<String> {

        private static final long serialVersionUID = 1619875355308645967L;

        public TextContributorType() {
            super(LongVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE, StandardBasicTypes.MATERIALIZED_CLOB.getName());
        }

    }

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        final Dialect dialect = serviceRegistry.getService(JdbcServices.class).getDialect();
        if (dialect instanceof org.hibernate.dialect.PostgreSQL81Dialect) {
            if (Boolean.getBoolean("org.kie.persistence.postgresql.useBytea")) {
                typeContributions.contributeType(new ByteaContributorType());
            }
            if (Boolean.getBoolean("org.kie.persistence.postgresql.useText")) {
                typeContributions.contributeType(new TextContributorType());
            }
        }

    }

}