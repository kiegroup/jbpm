/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.casemgmt.cmmn.xml;

import java.util.HashSet;

import org.drools.core.xml.BaseAbstractHandler;
import org.drools.core.xml.ExtensibleXmlParser;
import org.drools.core.xml.Handler;
import org.jbpm.casemgmt.cmmn.core.Definitions;
import org.jbpm.casemgmt.cmmn.core.FileItemDefinition;
import org.jbpm.casemgmt.cmmn.xml.util.CaseParserData;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FileItemDefinitionHandler extends BaseAbstractHandler implements Handler {

    public FileItemDefinitionHandler() {
        if ((this.validParents == null) && (this.validPeers == null)) {
            this.validParents = new HashSet<>();
            this.validParents.add(Definitions.class);
            this.validPeers = new HashSet<>();
            this.validPeers.add(null);
            this.validPeers.add(FileItemDefinition.class);
            this.validPeers.add(RuleFlowProcess.class);
            this.allowNesting = false;
        }
    }

    public Object start(final String uri,
                        final String localName,
                        final Attributes attrs,
                        final ExtensibleXmlParser parser) throws SAXException {
        CaseParserData data = CaseParserData.wrapParserMetadata(parser);
        
        parser.startElementBuilder(localName, attrs);

        String id = attrs.getValue("id");
        String name = attrs.getValue("name");
        String type = attrs.getValue("structureRef");
        if (type == null || type.trim().length() == 0) {
            type = "java.lang.Object";
        }

        FileItemDefinition itemDefinition = new FileItemDefinition(id);
        itemDefinition.setName(name);
        itemDefinition.setStructureRef(type);
        data.fileItemDefinitions.put(id, itemDefinition);
        return itemDefinition;
    }

    public Object end(final String uri,
                      final String localName,
                      final ExtensibleXmlParser parser) throws SAXException {
        parser.endElementBuilder();
        return parser.getCurrent();
    }

    public Class<?> generateNodeFor() {
        return FileItemDefinition.class;
    }

}
