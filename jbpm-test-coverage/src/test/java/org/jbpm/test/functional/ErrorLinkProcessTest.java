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

package org.jbpm.test.functional;

import org.jbpm.test.JbpmTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;


public class ErrorLinkProcessTest extends JbpmTestCase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    public static final String PROCESS_EMPTY = "org/jbpm/test/functional/common/EmptyLinkProcess.bpmn2";
    public static final String PROCESS_MULTI_THROW = "org/jbpm/test/functional/common/MultipleThrowLinkProcess.bpmn2";
    public static final String PROCESS_MULTI_CATCH = "org/jbpm/test/functional/common/MultipleCatchLinkProcess.bpmn2";
    public static final String PROCESS_UNCONNECTED = "org/jbpm/test/functional/common/UnconnectedLinkProcess.bpmn2";
    public static final String DIFFERENT_PROCESS = "org/jbpm/test/functional/common/DifferentLinkProcess.bpmn2";
    
    public ErrorLinkProcessTest() {
        super(false);
    }

    @Test
    public void testEmptyLinkEvents() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("nodes do not have a name");
        createKSession(PROCESS_EMPTY);
    }
    
    @Test
    public void testMultiThrowLinkEvents() {
        assertNotNull(createKSession(PROCESS_MULTI_THROW));
    }
    

    @Test
    public void testMultiCatchEvents() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("multiple catch nodes");
        createKSession(PROCESS_MULTI_CATCH);
    }
    
    @Test
    public void testUnconnectedEvents() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("not connection");
        createKSession(PROCESS_UNCONNECTED);
    }
    
    @Test
    public void testDifferentProcess() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("not connection");
        exceptionRule.expectMessage("subprocess");
        createKSession (DIFFERENT_PROCESS);
    }
}
