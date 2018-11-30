package org.jbpm.process.assembler;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.definition.process.Process;
import org.kie.api.internal.io.ResourceTypePackage;
import org.kie.api.io.ResourceType;

public class ProcessPackage implements ResourceTypePackage<Process> {

    private final Map<Object, Process> ruleFlows = new HashMap<>();
    private final ResourceType resourceType;

    public ProcessPackage(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Map<Object, Process> getRuleFlows() {
        return this.ruleFlows;
    }

    public void addProcess(Process process) {
        this.ruleFlows.put(process.getId(), process);
    }

    @Override
    public ResourceType getResourceType() {
        return this.resourceType;
    }

    @Override
    public String getNamespace() {
        return "$$PROCESS$$";
    }

    public Process lookup(String id) {
        return ruleFlows.get(id);
    }

    @Override
    public void add(Process processedResource) {
        getRuleFlows().put(processedResource.getId(), processedResource);
    }

    @Override
    public Iterable<? extends Process> contents() {
        return getRuleFlows().values();
    }
}
