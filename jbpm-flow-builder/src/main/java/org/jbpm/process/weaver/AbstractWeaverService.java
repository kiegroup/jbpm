package org.jbpm.process.weaver;

import java.util.Map;

import org.drools.core.definitions.InternalKnowledgePackage;
import org.jbpm.process.assembler.ProcessPackage;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.internal.weaver.KieWeaverService;
import org.kie.api.io.ResourceType;

public abstract class AbstractWeaverService implements KieWeaverService<ProcessPackage> {

    @Override
    public void merge(KieBase kieBase, KiePackage kiePkg, ProcessPackage processPkg) {
        Map<ResourceType, ResourceTypePackage> map = ((InternalKnowledgePackage)kiePkg).getResourceTypePackages();
        ResourceType resourceType = processPkg.getResourceType();
        ProcessPackage existing  = (ProcessPackage) map.get(resourceType);
        if ( existing == null ) {
            existing = new ProcessPackage(resourceType);
            map.put(resourceType, existing);
        }

        existing.getRuleFlows().putAll(processPkg.getRuleFlows());
    }

    @Override
    public void weave(KieBase kieBase, KiePackage kiePackage, ProcessPackage processPackage) {

    }

}
