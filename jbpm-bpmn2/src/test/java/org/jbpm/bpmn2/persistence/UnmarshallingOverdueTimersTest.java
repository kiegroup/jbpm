package org.jbpm.bpmn2.persistence;

import static junit.framework.Assert.assertTrue;
import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.createEnvironment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.bpmn2.concurrency.MultipleProcessesPerThreadTest;
import org.jbpm.persistence.util.PersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.persistence.jpa.JPAKnowledgeService;
import org.kie.runtime.Environment;
import org.kie.runtime.KieSessionConfiguration;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnmarshallingOverdueTimersTest {

    private static Logger logger = LoggerFactory.getLogger(MultipleProcessesPerThreadTest.class);

    private HashMap<String, Object> context;

    @Before
    public void setup() {
         context = PersistenceUtil.setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
    }

    @After
    public void tearDown() throws Exception {
        cleanUp(context);
    }

    private static KnowledgeBase loadKnowledgeBase(String bpmn2FileName) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(bpmn2FileName, UnmarshallingOverdueTimersTest.class), ResourceType.BPMN2);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

    private StatefulKnowledgeSession createStatefulKnowledgeSession(KnowledgeBase kbase) {
        Environment env = createEnvironment(context);
        return JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
    }

    private static int knowledgeSessionDispose(StatefulKnowledgeSession ksession) {
        int ksessionId = ksession.getId();
        logger.debug("disposing of ksesssion");
        ksession.dispose();
        return ksessionId;
    }

    private StatefulKnowledgeSession reloadStatefulKnowledgeSession(String bpmn2FileName, int ksessionId) {
        KnowledgeBase kbase = loadKnowledgeBase(bpmn2FileName);

        logger.debug(". reloading ksession " + ksessionId);
        Environment env = null;
        env = createEnvironment(context);

        return JPAKnowledgeService.loadStatefulKnowledgeSession(ksessionId, kbase, null, env);
    }


    
}
