package org.jbpm.formbuilder.server.task;

import org.drools.xml.ExtensibleXmlParser;
import org.drools.xml.Handler;
import org.jbpm.bpmn2.xml.PropertyHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessGetInputHandler extends PropertyHandler implements Handler {

        /**
         * To identify the form variables from process input.
         */
        private static final String PROCESS_INPUT_NAME = "startProcess";

        private final TaskRepoHelper taskRepository;
        
        public ProcessGetInputHandler(TaskRepoHelper taskRepository) {
                super();
                this.taskRepository = taskRepository;
        }

        @Override
        public Object start(final String uri, final String localName,
                        final Attributes attrs, final ExtensibleXmlParser parser)
                        throws SAXException {
                final String id = attrs.getValue("id");
                this.taskRepository.addOutput(PROCESS_INPUT_NAME, id);
                return super.start(uri, localName, attrs, parser);
        }

}