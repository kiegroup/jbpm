package org.jbpm.formbuilder.server.task;

import org.jbpm.bpmn2.xml.BPMNSemanticModule;

public class TaskDefinitionsSemanticModule extends BPMNSemanticModule {

    public static final String URI = "http://www.jboss.org/jbpm-form-builder";

    public TaskDefinitionsSemanticModule(TaskRepoHelper repo) {
        super();
        addHandler("userTask", new HumanTaskGetInformationHandler(repo));
        addHandler("property", new ProcessGetInputHandler(repo));
    }
}
