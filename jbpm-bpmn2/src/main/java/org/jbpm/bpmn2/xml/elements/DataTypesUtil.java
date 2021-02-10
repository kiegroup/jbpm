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

package org.jbpm.bpmn2.xml.elements;

import java.util.Map;

import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.process.core.datatype.DataType;
import org.jbpm.process.core.datatype.impl.type.BooleanDataType;
import org.jbpm.process.core.datatype.impl.type.FloatDataType;
import org.jbpm.process.core.datatype.impl.type.IntegerDataType;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.process.core.datatype.impl.type.StringDataType;

public final class DataTypesUtil {


    private DataTypesUtil() {
        // do nothing
    }

    public static DataType getDataType(Map<String, ItemDefinition> itemDefinitions, ClassLoader cl, String itemSubjectRef) {
        DataType dataType = new ObjectDataType();
        if (itemDefinitions == null) {
            return dataType;
        }
        ItemDefinition itemDefinition = itemDefinitions.get(itemSubjectRef);
        if (itemDefinition != null) {
            String structureRef = itemDefinition.getStructureRef();

            if ("java.lang.Boolean".equals(structureRef) || "Boolean".equals(structureRef)) {
                dataType = new BooleanDataType();

            } else if ("java.lang.Integer".equals(structureRef) || "Integer".equals(structureRef)) {
                dataType = new IntegerDataType();

            } else if ("java.lang.Float".equals(structureRef) || "Float".equals(structureRef)) {
                dataType = new FloatDataType();

            } else if ("java.lang.String".equals(structureRef) || "String".equals(structureRef)) {
                dataType = new StringDataType();

            } else if ("java.lang.Object".equals(structureRef) || "Object".equals(structureRef)) {
                dataType = new ObjectDataType(structureRef);

            } else {
                dataType = new ObjectDataType(structureRef, cl);
            }

        }
        return dataType;
    }
}
