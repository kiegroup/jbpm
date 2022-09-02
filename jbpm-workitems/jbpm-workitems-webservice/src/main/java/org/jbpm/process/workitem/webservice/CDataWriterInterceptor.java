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

import java.io.OutputStream;
import javax.xml.stream.XMLStreamWriter;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;

public class CDataWriterInterceptor extends AbstractPhaseInterceptor<Message>{

    private final String cdataElement;

    public CDataWriterInterceptor(String cdataElement) {
        super(Phase.PRE_STREAM);
        addAfter(AttachmentOutInterceptor.class.getName());
        this.cdataElement = cdataElement;
    }

    @Override
    public void handleMessage(Message message) {
        OutputStream msg = message.getContent(OutputStream.class);
        if (msg == null) {
            return;
        }
        
        message.put("disable.outputstream.optimization", Boolean.TRUE);
        message.setContent(XMLStreamWriter.class, new CDataXMLStreamWriter(StaxUtils.createXMLStreamWriter(msg), cdataElement));
    }
}
