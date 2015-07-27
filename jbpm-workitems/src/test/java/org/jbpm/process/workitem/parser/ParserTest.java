package org.jbpm.process.workitem.parser;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class ParserTest {

	final int AGE = 27;
	final String NAME = "William";
	final String PERSON_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><age>"
			+ AGE + "</age><name>" + NAME + "</name></person>";
	final String PERSON_JSON = "{\"name\":\"" + NAME + "\",\"age\":" + AGE
			+ "}";

	Parser handler;

	@Before
	public void init() {
		handler = new Parser();
	}

	@Test
	public void testXmlToObject() {
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter(Parser.INPUT, PERSON_XML);
		workItem.setParameter(Parser.FORMAT, Parser.XML);
		workItem.setParameter(Parser.TYPE,
				"org.jbpm.process.workitem.parser.Person");
		handler.executeWorkItem(workItem, new TestWorkItemManager(workItem));
		Person result = (Person) workItem.getResult(Parser.RESULT);
		assertEquals(AGE, result.getAge());
		assertEquals(NAME, result.getName());
	}

	@Test
	public void testObjectToXml() {
		Person p = new Person(NAME, AGE);
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter(Parser.INPUT, p);
		workItem.setParameter(Parser.FORMAT, Parser.XML);
		handler.executeWorkItem(workItem, new TestWorkItemManager(workItem));
		String result = (String) workItem.getResult(Parser.RESULT);
		assertEquals(PERSON_XML, result);
	}

	@Test
	public void testJsonToObject() {
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter(Parser.INPUT, PERSON_JSON);
		workItem.setParameter(Parser.FORMAT, Parser.JSON);
		workItem.setParameter(Parser.TYPE,
				"org.jbpm.process.workitem.parser.Person");
		handler.executeWorkItem(workItem, new TestWorkItemManager(workItem));
		Person result = (Person) workItem.getResult(Parser.RESULT);
		assertEquals(AGE, result.getAge());
		assertEquals(NAME, result.getName());
	}

	public void testObjectToJson() {
		Person p = new Person(NAME, AGE);
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setParameter(Parser.INPUT, p);
		workItem.setParameter(Parser.FORMAT, Parser.JSON);
		handler.executeWorkItem(workItem, new TestWorkItemManager(workItem));
		String result = (String) workItem.getResult(Parser.RESULT);
		assertEquals(PERSON_JSON, result);
	}

	private class TestWorkItemManager implements WorkItemManager {

		private WorkItem workItem;

		TestWorkItemManager(WorkItem workItem) {
			this.workItem = workItem;
		}

		public void completeWorkItem(long id, Map<String, Object> results) {
			((WorkItemImpl) workItem).setResults(results);

		}

		public void abortWorkItem(long id) {

		}

		public void registerWorkItemHandler(String workItemName,
				WorkItemHandler handler) {

		}

	}

}
