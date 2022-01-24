/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.persistence.types;

import java.math.BigInteger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jbpm.test.persistence.scripts.DatabaseType;
import org.jbpm.test.persistence.scripts.PersistenceUnit;
import org.jbpm.test.persistence.scripts.TestPersistenceContextBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


public class PosgresqlTypesTest {

    public final static int STRLENGTH = 1000000;

    private TestPersistenceContextBase persistenceContext;

    @Before
    public void setup() {
        System.setProperty("org.kie.persistence.postgresql.useBytea", "true");
        System.setProperty("org.kie.persistence.postgresql.useText", "true");
        persistenceContext = new TestPersistenceContextBase();
        persistenceContext.init(PersistenceUnit.TEST_TYPES);
        Assume.assumeTrue(persistenceContext.getDatabaseType().equals(DatabaseType.POSTGRESQL));
    }

    @After
    public void tear() {
        System.clearProperty("org.kie.persistence.postgresql.useBytea");
        System.clearProperty("org.kie.persistence.postgresql.useText");
        persistenceContext.clean();
    }

    @Test
    public void testCreateEntityByteArray() {
        persistenceContext.getTransactionManager().begin();

        int pgCount = getPgLargeObjectCounter();

        TestEntity testEntity = new TestEntity();
        testEntity.setProcessInstanceByteArray(getLongString().getBytes());

        EntityManager em = persistenceContext.getEntityManagerFactory().createEntityManager();
        try {
            em.persist(testEntity);
        } finally {
            em.close();
        }
        
        persistenceContext.getTransactionManager().commit(true);

        persistenceContext.getTransactionManager().begin();
        Assert.assertEquals(pgCount, getPgLargeObjectCounter());
        persistenceContext.getTransactionManager().commit(true);

    }

    @Test
    public void testCreateEntityErrorInfo() {
        persistenceContext.getTransactionManager().begin();
        int pgCount = getPgLargeObjectCounter();

        TestEntity testEntity = new TestEntity();
        testEntity.setErrorInfo(getLongString());
        EntityManager em = persistenceContext.getEntityManagerFactory().createEntityManager();
        try {
            em.persist(testEntity);
        } finally {
            em.close();
        }

        persistenceContext.getTransactionManager().commit(true);

        persistenceContext.getTransactionManager().begin();
        Assert.assertEquals(pgCount, getPgLargeObjectCounter());
        persistenceContext.getTransactionManager().commit(true);
    }

    private String getLongString() {
        StringBuilder dot = new StringBuilder();
        for(int i = 0; i < STRLENGTH; i++) {
            dot.append(".");
        }
        return dot.toString();
    }

    private int getPgLargeObjectCounter() {
        EntityManager em = persistenceContext.getEntityManagerFactory().createEntityManager();
        try {
            return getPgLargeObjectCounter(em);
        } finally {
            em.close();
        }
    }
    private int getPgLargeObjectCounter(EntityManager session) {
        Query query = session.createNativeQuery("select count(*) from pg_largeobject_metadata");
        return ((BigInteger) query.getSingleResult()).intValue();
    }
}