/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.assembler;

import java.util.List;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.compiler.BaseKnowledgeBuilderResultImpl;
import org.drools.compiler.compiler.ProcessLoadError;
import org.drools.core.impl.InternalKnowledgeBase;
import org.jbpm.compiler.ProcessBuilderImpl;
import org.kie.api.definition.process.Process;
import org.kie.api.internal.assembler.KieAssemblerService;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceConfiguration;
import org.kie.api.io.ResourceType;

public abstract class AbstractProcessAssembler implements KieAssemblerService {

    public void addResource(
            Object kbuilder,
            Resource resource,
            ResourceType type,
            ResourceConfiguration configuration) throws Exception {

        KnowledgeBuilderImpl kb = (KnowledgeBuilderImpl) kbuilder;
        ProcessBuilderImpl processBuilder = (ProcessBuilderImpl) kb.getProcessBuilder();
        configurePackageBuilder(kb);

        try {
            List<Process> processes = processBuilder.addProcessFromXml(resource);
            List<BaseKnowledgeBuilderResultImpl> errors = processBuilder.getErrors();
            if (errors.isEmpty()) {
                InternalKnowledgeBase kBase = kb.getKnowledgeBase();
                if (kBase != null && processes != null) {
                    for (Process process : processes) {
                        kBase.addProcess(process);
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
