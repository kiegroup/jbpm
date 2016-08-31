/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.test.util;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.integrationtests.JbpmSerializationHelper;
import org.jbpm.process.test.TestProcessEventListener;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.test.util.logging.LoggingPrintStream;

public abstract class AbstractBaseTest {

    protected KnowledgeBuilderImpl builder;

    protected static final String OLD_RECURSIVE_STACK = "RECURSIVE";
    protected static final String QUEUE_BASED_EXECUTION =  "QUEUE-BASED";

    protected boolean queueBasedExecution = false;

    protected static Collection<Object[]> getQueueBasedTestOptions() {
        Object[][] execModelType = new Object[][] {
            { QUEUE_BASED_EXECUTION },
            { OLD_RECURSIVE_STACK }
            };
            return Arrays.asList(execModelType);
    }

    public AbstractBaseTest() {
        // Default constructor
    }

    public AbstractBaseTest(String execModel) {
        this.queueBasedExecution = QUEUE_BASED_EXECUTION.equals(execModel);
    }

    @Before
    public void before() {
        builder = new KnowledgeBuilderImpl();
    }

    public KieSession createKieSession(KnowledgePackage... pkg) {
        try {
            return createKieSession(false, pkg);
        } catch(Exception e ) {
            String msg = "There's no reason fo an exception to be thrown here (because the kbase is not being serialized)!";
            fail( msg );
            throw new RuntimeException(msg, e);
        }
    }

    public KieSession createKieSession(boolean serializeKbase, KnowledgePackage... pkg) throws Exception {
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages((Collection) Arrays.asList(pkg));
        if( serializeKbase ) {
            kbase = JbpmSerializationHelper.serializeObject( kbase );
        }

        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.USE_QUEUE_BASED_EXECUTION, queueBasedExecution);
        KieSession ksession = kbase.newKieSession(null, env);

        ksession.addEventListener(new TestProcessEventListener());
        return ksession;
    }

    @BeforeClass
    public static void configure() {
        LoggingPrintStream.interceptSysOutSysErr();
    }

    @AfterClass
    public static void reset() {
        LoggingPrintStream.restoreSysOutAndSysErr();
    }
}
