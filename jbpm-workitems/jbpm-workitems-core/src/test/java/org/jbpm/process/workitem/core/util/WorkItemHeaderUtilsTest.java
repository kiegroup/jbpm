/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.workitem.core.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kie.api.runtime.process.WorkItem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkItemHeaderUtilsTest {

	@Test
	public void testBuildHeaderList() {
		WorkItem workItem = mock(WorkItem.class);
		Map<String, Object> map = new HashMap<>();
		map.put("HEADER_Pepito_Grillo", "fulanito");
		map.put("header_param_NS_Pepito_Grillo", "http://pepito.com");
		map.put("mamotreco", "power");
		when(workItem.getParameters()).thenReturn(map);
		Collection<WorkItemHeaderInfo> headers = WorkItemHeaderUtils.getHeaderInfo(workItem);
		assertEquals(1, headers.size());
		WorkItemHeaderInfo header = headers.iterator().next();
		assertEquals("Pepito_Grillo", header.getName());
		assertEquals("fulanito", header.getContent());
		assertEquals("http://pepito.com", header.getParam("NS"));
	}

	@Test
	public void testBuildHeaderListWithCustomSeparator() {
		System.setProperty(WorkItemHeaderUtils.SEPARATOR_PROP, "//");
		try {
			WorkItem workItem = mock(WorkItem.class);
			Map<String, Object> map = new HashMap<>();
			map.put("HEADER_Pepito_Grillo", "fulanito");
			map.put("header_param_NS_232//Pepito_Grillo", "http://pepito.com");
			map.put("mamotreco", "power");
			when(workItem.getParameters()).thenReturn(map);
			Collection<WorkItemHeaderInfo> headers = WorkItemHeaderUtils.getHeaderInfo(workItem);
			assertEquals(1, headers.size());
			WorkItemHeaderInfo header = headers.iterator().next();
			assertEquals("Pepito_Grillo", header.getName());
			assertEquals("fulanito", header.getContent());
			assertEquals("http://pepito.com", header.getParam("NS_232"));
		} finally {
			System.clearProperty(WorkItemHeaderUtils.SEPARATOR_PROP);
		}
	}

}
