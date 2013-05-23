/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.workitem.archive.ArchiveWorkItemHandler;
import org.jbpm.process.workitem.email.EmailWorkItemHandler;
import org.jbpm.process.workitem.exec.ExecWorkItemHandler;
import org.jbpm.process.workitem.finder.FinderWorkItemHandler;
import org.jbpm.process.workitem.transform.FileTransformer;
import org.jbpm.process.workitem.transform.TransformWorkItemHandler;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * This is a sample file to launch a ruleflow.
 */
@Ignore
public class RuleFlowTest {

	public static final void main(String[] args) {
		try {
			
			KnowledgeBase kbase = createKnowledgeBase();
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			ksession.getWorkItemManager().registerWorkItemHandler("Finder", new FinderWorkItemHandler());
			ksession.getWorkItemManager().registerWorkItemHandler("Archive", new ArchiveWorkItemHandler());
			ksession.getWorkItemManager().registerWorkItemHandler("Exec", new ExecWorkItemHandler());
			ksession.getWorkItemManager().registerWorkItemHandler("Log", new WorkItemHandler() {
				public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
					System.out.println("Log: " + workItem.getParameter("Message"));
					manager.completeWorkItem(workItem.getId(), null);
				}
				public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
				}
			});
			EmailWorkItemHandler emailWorkItemHandler = new EmailWorkItemHandler();
			emailWorkItemHandler.setConnection("mail-out.example.com", "25", null, null);
			ksession.getWorkItemManager().registerWorkItemHandler("Email", emailWorkItemHandler);
			TransformWorkItemHandler transformWorkItemHandler = new TransformWorkItemHandler();
			transformWorkItemHandler.registerTransformer(FileTransformer.class);
			ksession.getWorkItemManager().registerWorkItemHandler("Transform", transformWorkItemHandler);
			KnowledgeRuntimeLogger log = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");
			ksession.startProcess("com.sample.ruleflow");
			log.close();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static KnowledgeBase createKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(new ClassPathResource("/FileFinder.rf"), ResourceType.DRF);
		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
		return kbase;
	}



    @Test
    public void testStatelessSessionRuleflowState() {

        String rf = "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                    "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                    "         type=\"RuleFlow\" name=\"flow\" id=\"growth\" package-name=\"com.sample\" >\n" +
                    "\n" +
                    "  <header>\n" +
                    "  </header>\n" +
                    "\n" +
                    "  <nodes>\n" +
                    "    <start id=\"1\" name=\"Start\" />\n" +
                    "    <ruleSet id=\"2\" name=\"A\" ruleFlowGroup=\"A\" />\n" +
                    "    <ruleSet id=\"3\" name=\"B\" ruleFlowGroup=\"B\" />\n" +
                    "    <end id=\"4\" name=\"End\"  />\n" +
                    "  </nodes>\n" +
                    "\n" +
                    "  <connections>\n" +
                    "    <connection from=\"1\" to=\"2\" />\n" +
                    "    <connection from=\"2\" to=\"3\" />\n" +
                    "    <connection from=\"3\" to=\"4\" />\n" +
                    "  </connections>\n" +
                    "\n" +
                    "</process>";

        String drl = "package com.sample; \n" +
                     "global java.util.List list; \n" +
                     "\n" +
                     "\n" +
                     "        rule \"A\"\n" +
                     "        ruleflow-group \"A\"\n" +
                     "        when        \n" +
                     "            $x: String() \n" +
                     "        then\n" +
                     "            list.add( \"A\" );\n" +
                     "        end\n" +
                     "\n" +
                     "        rule \"B\"\n" +
                     "        ruleflow-group \"B\"\n" +
                     "        when        \n" +
                     "            $y: String() \n" +
                     "        then\n" +
                     "            list.add( \"B\" );\n" +
                     "        end\n";


        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( drl.getBytes() ), ResourceType.DRL);
        kbuilder.add(ResourceFactory.newByteArrayResource( rf.getBytes() ), ResourceType.DRF);
        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List list;

        list = new ArrayList(  );
        ksession.execute( CommandFactory.newBatchExecution( Arrays.asList(
                CommandFactory.newSetGlobal( "list", list ),
                CommandFactory.newInsert( "a" ),
                CommandFactory.newStartProcess( "growth" )
        ) ) );
        assertEquals( 2, list.size() );
        assertTrue( list.contains( "A" ) );
        assertTrue( list.contains( "B" ) );

        list = new ArrayList(  );
        ksession.execute( CommandFactory.newBatchExecution( Arrays.asList(
                CommandFactory.newSetGlobal( "list", list ),
                CommandFactory.newInsert( "b" ),
                CommandFactory.newStartProcess( "growth" )
        ) ) );
        assertEquals( 2, list.size() );
        assertTrue( list.contains( "A" ) );
        assertTrue( list.contains( "B" ) );

        list = new ArrayList(  );
        ksession.execute( CommandFactory.newBatchExecution( Arrays.asList(
                CommandFactory.newSetGlobal( "list", list ),
                CommandFactory.newInsert( "c" ),
                CommandFactory.newStartProcess( "growth" )
        ) ) );
        assertEquals( 2, list.size() );
        assertTrue( list.contains( "A" ) );
        assertTrue( list.contains( "B" ) );


    }




}