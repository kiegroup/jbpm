/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.test.regression;

import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.ListenerModel;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import qa.tools.ikeeper.annotation.BZ;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests stateful/stateless KieSession ProcessEventListener registration - DROOLS-818.
 */
public class ListenersTest extends JbpmTestCase {

    private static final ReleaseId RELEASE_ID = KieServices.Factory.get()
            .newReleaseId("org.jbpm.test", "listeners-test", "1.0.0");

    private KieServices ks = KieServices.Factory.get();
    private KieSession kieSession;
    private StatelessKieSession statelessKieSession;

    public ListenersTest() {
        super(false);
    }

    @Before
    public void init() {
        ReleaseId kieModuleId = prepareKieModule();

        final KieContainer kieContainer = ks.newKieContainer(kieModuleId);
        this.kieSession = kieContainer.newKieSession((Environment) null, null);
        this.statelessKieSession = kieContainer.newStatelessKieSession(ks.newKieSessionConfiguration());
    }

    @After
    public void cleanup() {
        if (this.kieSession != null) {
            this.kieSession.dispose();
        }
        this.statelessKieSession = null;
    }

    @Test
    public void testRegisterProcessEventListenerStateful() throws Exception {
        kieSession.startProcess("testProcess");
        checkThatListenerFired(kieSession.getProcessEventListeners());
    }

    @BZ("1233197")
    @Test
    public void testRegisterProcessEventListenerStateless() throws Exception {
        statelessKieSession.execute(KieServices.Factory.get().getCommands().newStartProcess("testProcess"));
        checkThatListenerFired(statelessKieSession.getProcessEventListeners());
    }

    private void checkThatListenerFired(Collection listeners) {
        assertTrue("Listener not registered.", listeners.size() >= 1);
        MarkingProcessEventListener listener = getMarkingListener(listeners);
        assertTrue("Expected listener to fire.", listener.hasFired());
    }

    private MarkingProcessEventListener getMarkingListener(Collection listeners) {
        for (Object listener : listeners) {
            if (listener instanceof MarkingProcessEventListener) {
                return (MarkingProcessEventListener) listener;
            }
        }
        throw new IllegalArgumentException("Expected at least one MarkingProcessEventListener in the collection");
    }

    /**
     * Inserts a new KieModule containing single KieBase and a stateful and stateless KieSessions with listeners
     * into KieRepository.
     *
     * @return created module ReleaseId
     */
    private ReleaseId prepareKieModule() {
        final KieServices ks = KieServices.Factory.get();

        KieModuleModel module = ks.newKieModuleModel();

        KieBaseModel baseModel = module.newKieBaseModel("defaultKBase");
        baseModel.setDefault(true);
        baseModel.addPackage("*");

        KieSessionModel sessionModel = baseModel.newKieSessionModel("defaultKSession");
        sessionModel.setDefault(true);
        sessionModel.setType(KieSessionModel.KieSessionType.STATEFUL);
        sessionModel.newListenerModel(MarkingProcessEventListener.class.getName(), ListenerModel.Kind.PROCESS_EVENT_LISTENER);

        KieSessionModel statelessSessionModel = baseModel.newKieSessionModel("defaultStatelessKSession");
        statelessSessionModel.setDefault(true);
        statelessSessionModel.setType(KieSessionModel.KieSessionType.STATELESS);
        statelessSessionModel.newListenerModel(MarkingProcessEventListener.class.getName(), ListenerModel.Kind.PROCESS_EVENT_LISTENER);

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(module.toXML());
        kfs.generateAndWritePomXML(RELEASE_ID);

        kfs.write(ResourceFactory.newClassPathResource("listeners-test.bpmn", this.getClass()));

        KieBuilder builder = ks.newKieBuilder(kfs).buildAll();
        assertEquals("Unexpected compilation errors", 0, builder.getResults().getMessages().size());

        ks.getRepository().addKieModule(builder.getKieModule());

        return RELEASE_ID;
    }

    /**
     * A listener marking that a ProcessEvent has fired.
     */
    public static class MarkingProcessEventListener extends DefaultProcessEventListener {

        private final AtomicBoolean fired = new AtomicBoolean(false);

        @Override
        public void beforeProcessStarted(final ProcessStartedEvent event) {
            super.beforeProcessStarted(event);
            this.fired.compareAndSet(false, true);
        }

        public boolean hasFired() {
            return this.fired.get();
        }
    }
}