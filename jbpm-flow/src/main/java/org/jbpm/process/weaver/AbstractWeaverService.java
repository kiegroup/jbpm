package org.jbpm.process.weaver;

import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.ProcessPackage;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.internal.weaver.KieWeaverService;

public abstract class AbstractWeaverService implements KieWeaverService<ProcessPackage> {

    @Override
    public void merge(KieBase kieBase, KiePackage kiePkg, ProcessPackage processPkg) {
        ProcessPackage existing = ((InternalKnowledgePackage) kiePkg).getResourceTypePackages().computeIfAbsent(
                processPkg.getResourceType(), ProcessPackage::new);

        existing.getRuleFlows().putAll(processPkg.getRuleFlows());
    }

    @Override
    public void weave(KieBase kieBase, KiePackage kiePackage, ProcessPackage processPackage) {

    }

}
