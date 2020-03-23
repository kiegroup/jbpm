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

package org.jbpm.persistence.correlation;

import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.jbpm.test.util.AbstractBaseTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;

import static org.jbpm.test.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.test.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.test.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CorrelationKeySizePersistenceTest extends AbstractBaseTest {

    private HashMap<String, Object> context;

    private String correlationKey;

    @Before
    public void before() throws Exception {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
        correlationKey = "";
        for (int i = 0; i < 300; i++) {
            correlationKey += (i % 10);
        }
    }

    @After
    public void after() throws Exception {
        EntityManagerFactory emf = (EntityManagerFactory) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        ut.begin();
        try {
            EntityManager em = emf.createEntityManager();
            em.createQuery("delete from CorrelationPropertyInfo").executeUpdate();
            em.createQuery("delete from CorrelationKeyInfo").executeUpdate();
            ut.commit();
        } catch (Exception ex) {
            ut.rollback();
            Assert.fail("Exception thrown while trying to cleanup correlation data.");
        }
        cleanUp(context);
    }

    @Test
    public void testCreateCorrelationPropertyInfo() throws Exception {
        CorrelationKeyFactory factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
        // populate table with test data
        EntityManagerFactory emf = (EntityManagerFactory) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        ut.begin();
        try {
            EntityManager em = emf.createEntityManager();
            em.persist(factory.newCorrelationKey(correlationKey));
            ut.commit();
        } catch (Exception ex) {
            ut.rollback();
            Assert.fail("Exception thrown while trying to prepare correlation data.");
        }

        EntityManager em = emf.createEntityManager();

        TypedQuery<CorrelationPropertyInfo> query = em.createQuery("SELECT o FROM CorrelationPropertyInfo o WHERE o.value = :ckey", CorrelationPropertyInfo.class);
        // we checked with the trimmed value of the correlation key
        query.setParameter("ckey", correlationKey.substring(0, 255));

        List<CorrelationPropertyInfo> keyInfo = query.getResultList();

        em.close();
        assertNotNull(keyInfo);
        assertEquals(1, keyInfo.size());
    }

}
