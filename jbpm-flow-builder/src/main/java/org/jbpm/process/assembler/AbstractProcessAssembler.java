package org.jbpm.process.assembler;

import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.BPMN2ProcessFactory;
import org.drools.compiler.compiler.BaseKnowledgeBuilderResultImpl;
import org.drools.compiler.compiler.ProcessBuilder;
import org.drools.compiler.compiler.ProcessLoadError;
import org.drools.core.impl.InternalKnowledgeBase;
import org.kie.api.cdi.KBase;
import org.kie.api.definition.process.Process;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.ResourceChange;

public abstract class AbstractProcessAssembler implements KieAssemblerService {

    public void addResource(
            Object kbuilder,
            Resource resource,
            ResourceType type,
            ResourceConfiguration configuration) throws Exception {

        KnowledgeBuilderImpl kb = (KnowledgeBuilderImpl) kbuilder;
        ProcessBuilder processBuilder = kb.getProcessBuilder();
        configurePackageBuilder(kb);
        processBuilder.addProcessFromXml(resource);

        InternalKnowledgeBase kBase = kb.getKnowledgeBase();

        try {
            List<Process> processes = processBuilder.addProcessFromXml(resource);
            List<BaseKnowledgeBuilderResultImpl> errors = processBuilder.getErrors();
            if (errors.isEmpty()) {
                if (kBase != null && processes != null) {
                    for (Process process : processes) {
                        //if (kb.filterAccepts(ResourceChange.Type.PROCESS, process.getNamespace(), process.getId())) {
                            kBase.addProcess(process);
                        //}
                    }
                }
            } else {
                errors.forEach(kb::addBuilderResult);
                errors.clear();
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            kb.addBuilderResult(new ProcessLoadError(resource, "Unable to load process.", e));
        }



    }

    protected abstract void configurePackageBuilder(KnowledgeBuilderImpl kb);

}
