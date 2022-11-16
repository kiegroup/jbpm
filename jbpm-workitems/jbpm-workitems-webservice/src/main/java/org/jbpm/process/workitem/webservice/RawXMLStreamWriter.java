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

import java.util.Collection;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.cxf.staxutils.DelegatingXMLStreamWriter;
import org.codehaus.stax2.XMLStreamWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawXMLStreamWriter extends DelegatingXMLStreamWriter {

    private static Logger logger = LoggerFactory.getLogger(RawXMLStreamWriter.class);
    private final Collection<String> rawElements;
    private String currentElementName;

    public RawXMLStreamWriter(XMLStreamWriter del, Collection<String> rawElements) {
        super(del);
        this.rawElements = rawElements;
    }

    @Override
    public void writeStartElement(String prefix, String local, String uri) throws XMLStreamException {
        currentElementName = local; 
        super.writeStartElement(prefix, local, uri); 
    } 

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        if (delegate instanceof XMLStreamWriter2 && rawElements.contains(currentElementName)) { 
          logger.debug("Writing Raw for element {}", currentElementName);
          ((XMLStreamWriter2) delegate).writeRaw(text, start, len);
        } else {
            logger.debug("No Raw replacement for element {} for writer {}", currentElementName, delegate);
            super.writeCharacters(text, start, len);
        }
    }
}
