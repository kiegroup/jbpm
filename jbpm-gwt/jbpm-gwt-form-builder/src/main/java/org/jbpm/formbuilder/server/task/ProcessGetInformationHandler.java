package org.jbpm.formbuilder.server.task;

import org.drools.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.xml.ProcessHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessGetInformationHandler extends ProcessHandler {

    private final TaskRepoHelper taskRepository;
    
    public ProcessGetInformationHandler(TaskRepoHelper taskRepository) {
            super();
            this.taskRepository = taskRepository;
    }

    @Override
    public Object start(String uri, String localName, Attributes attrs,
            ExtensibleXmlParser parser) throws SAXException {
        final String processId = attrs.getValue("id");
        final String processName = attrs.getValue("name");
        final String packageName = attrs.getValue("http://www.jboss.org/drools", "packageName");
        this.taskRepository.setDefaultProcessId(processId);
        this.taskRepository.setDefaultProcessName(processName);
        this.taskRepository.setDefaultPackageName(packageName);
        return super.start(uri, localName, attrs, parser);
    }

}
