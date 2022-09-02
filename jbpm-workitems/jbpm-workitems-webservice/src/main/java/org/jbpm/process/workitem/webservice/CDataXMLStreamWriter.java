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

package org.jbpm.process.workitem.webservice;

import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.cxf.staxutils.DelegatingXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDataXMLStreamWriter extends DelegatingXMLStreamWriter {

    private static Logger logger = LoggerFactory.getLogger(CDataXMLStreamWriter.class);
    private final List<String> cdataElements;
    private String currentElementName;

    public CDataXMLStreamWriter(XMLStreamWriter del, String cdataElementStr) {
        super(del);
        this.cdataElements = Arrays.asList(cdataElementStr.split(","));
    }

    @Override
    public void writeStartElement(String prefix, String local, String uri) throws XMLStreamException {
        currentElementName = local; 
        super.writeStartElement(prefix, local, uri); 
    } 

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        if (cdataElements.contains(currentElementName)) { 
          logger.debug("Writing CDATA for element {}", currentElementName);
          super.writeCData(new String(text));
        } else {
            logger.debug("No CDATA replacement for element {}", currentElementName);
            super.writeCharacters(text, start, len);
        }
    }
}
