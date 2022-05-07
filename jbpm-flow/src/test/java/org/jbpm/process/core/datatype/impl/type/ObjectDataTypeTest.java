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

package org.jbpm.process.core.datatype.impl.type;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectDataTypeTest {

    @Test
    public void testDateObject(){
        ObjectDataType objectDataType = new ObjectDataType("java.time.LocalDateTime");

        String dateTimeString = "2022-05-09T00:00";
        java.time.LocalDateTime dateTimeObject = java.time.LocalDateTime.parse(dateTimeString);
        String notDateTime = "202205-09T00:00";
        assertEquals(true, objectDataType.verifyDataType(dateTimeString));
        assertEquals(true, objectDataType.verifyDataType(dateTimeObject));
        assertEquals(false, objectDataType.verifyDataType(notDateTime));
    }
}
