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

import java.io.IOException;
import java.io.OutputStream;

import org.jbpm.workflow.core.node.DataAssociation;

public class DataOutputAssociationWriter implements ElementWriter<DataAssociation> {


    @Override
    public void write(OutputStream os, DataAssociation element) throws IOException {
        os.write(("      <dataOutputAssociation>" + ElementConstants.EOL).getBytes());
        for(String source : element.getSources()) {
            os.write(("        <sourceRef>_" + element.getUuid().toString() + "_" + source + "_Output</sourceRef>" + ElementConstants.EOL).getBytes());
        }
        os.write(("        <targetRef>" + element.getTarget() + "</targetRef>" + ElementConstants.EOL).getBytes());
        os.write(("      </dataOutputAssociation>" + ElementConstants.EOL).getBytes());
    }

}
