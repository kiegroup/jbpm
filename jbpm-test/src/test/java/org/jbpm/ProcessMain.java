package org.jbpm;

import org.jbpm.test.JBPMHelper;
import org.kie.KieBase;
import org.kie.KieServices;
import org.kie.builder.KieBuilder;
import org.kie.builder.KieFileSystem;
import org.kie.builder.KieRepository;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.Message.Level;
import org.kie.io.ResourceFactory;
import org.kie.io.ResourceType;
import org.kie.runtime.KieContainer;
import org.kie.runtime.StatefulKnowledgeSession;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMain {

    public static final void main(String[] args) throws Exception {
        startUp();
        // load up the knowledge base
        KieBase kbase = readKnowledgeBase();
        StatefulKnowledgeSession ksession = JBPMHelper
                .newStatefulKnowledgeSession(kbase);
        // start a new process instance
        ksession.startProcess("com.sample.bpmn.hello");
        System.out.println("Process started ...");
    }

    private static KieBase readKnowledgeBase() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(ResourceFactory.newClassPathResource("humantask.bpmn"));

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll(); // kieModule is automatically deployed to KieRepository
                       // if successfully built.
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n"
                    + kb.getResults().toString());
        }

        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        return kContainer.getKieBase();
    }

    private static void startUp() {
        JBPMHelper.startH2Server();
        JBPMHelper.setupDataSource();
        // please comment this line if you already have the task service
        // running,
        // for example when running the jbpm-installer
        JBPMHelper.startTaskService();
    }

}