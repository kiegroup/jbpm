/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.core.context.variable;

import java.util.Collection;
import java.util.Objects;

import org.drools.core.ClassObjectFilter;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.CaseData;
import org.kie.api.runtime.rule.FactHandle;

public class CaseVariableInstance<T> implements VariableInstance<T> {

    private final InternalKnowledgeRuntime knowledgeRuntime;
    private final Variable variable;
    private final String caseFileName;
    private final ValueReference<T> valueReference = new ValueReference<T>() {
        @Override
        public T get() {
            Collection<CaseData> caseFiles = (Collection<CaseData>)
                    knowledgeRuntime.getObjects(new ClassObjectFilter(CaseData.class));
            if (caseFiles.size() == 1) {
                CaseData caseFile = caseFiles.iterator().next();
                // check if there is case file prefix and if so remove it before checking case file data
                if (caseFile != null) {
                    return (T) caseFile.getData(caseFileName);
                }
            }
            return null;
        }

        @Override
        public void set(T value) {
            Collection<CaseData> caseFiles = (Collection<CaseData>)
                    knowledgeRuntime.getObjects(new ClassObjectFilter(CaseData.class));
            if (caseFiles.size() == 1) {
                CaseData caseFile = caseFiles.iterator().next();
                FactHandle factHandle = knowledgeRuntime.getFactHandle(caseFile);
                Objects.requireNonNull(factHandle, "factHandle for " + caseFileName + " was null");
                caseFile.add(caseFileName, value);
                knowledgeRuntime.update(factHandle, caseFile);
                ((KieSession) knowledgeRuntime).fireAllRules();
            }
        }
    };

    public CaseVariableInstance(VariableScopeInstance parentScopeInstance, Variable variable) {
        this.knowledgeRuntime = parentScopeInstance.getProcessInstance().getKnowledgeRuntime();
        this.variable = variable;
        if (variable.getName().startsWith(VariableScope.CASE_FILE_PREFIX)) {
            this.caseFileName = variable.getName().substring(VariableScope.CASE_FILE_PREFIX.length());
        } else {
            // should probably issue a warning
            this.caseFileName = variable.getName();
        }
    }

    @Override
    public T get() {
        return valueReference.get();
    }

    @Override
    public void set(T value) {
        valueReference.set(value);
    }

    @Override
    public void setReference(ValueReference<T> value) {
        // shall we log this? "Cannot setReference on a CaseVariableInstance". will set(value.get())
        set(value.get());
    }

    @Override
    public ValueReference<T> getReference() {
        // fixme: is a CaseVariable really an InstanceVariable? Shall we use a ValueReference?
//        throw new UnsupportedOperationException("Cannot get reference from a Case variable");
        return valueReference;
    }

    @Override
    public String name() {
        return variable.getName();
    }
}
