/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.builder.dialect.mvel;

import org.jbpm.process.core.impl.ObjectCloner;
import org.jbpm.process.core.impl.ObjectCloner.Config;
import org.jbpm.workflow.instance.NodeInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceResolverFactory;
import org.mvel2.integration.VariableResolver;

public class MVELResolverFactory extends NodeInstanceResolverFactory {

    private static final long serialVersionUID = 1L;
    private static final String CLONE_COLLECTION_PROP = "org.kie.jbpm.mvel.notCloneCollections";
    private String varName;

    public MVELResolverFactory(NodeInstance nodeInstance, String varName) {
        super(nodeInstance);
        this.varName = varName;
    }

    private transient Object clonedValue;

    public Object getVariable() {
        return clonedValue;
    }

    @Override
    public VariableResolver getVariableResolver(String name) {
        return isLeftValue(name) ? new CloningVariableResolver(super.getVariableResolver(name)) : super.getVariableResolver(name);
    }

    private boolean isLeftValue(String name) {
        return varName.equals(name) || varName.startsWith(name + ".");
    }

    private class CloningVariableResolver implements VariableResolver {

        private static final long serialVersionUID = 1L;

        private final VariableResolver resolver;

        public CloningVariableResolver(VariableResolver resolver) {
            this.resolver = resolver;
        }

        @Override
        public String getName() {
            return resolver.getName();
        }

        @Override
        public Class<?> getType() {
            return resolver.getType();
        }

        @Override
        public void setStaticType(Class type) {
            resolver.setStaticType(type);

        }

        @Override
        public int getFlags() {
            return resolver.getFlags();
        }

        @Override
        public Object getValue() {
            if (clonedValue == null) {
                clonedValue = ObjectCloner.clone(resolver.getValue(), new Config().deepCloneCollections(!Boolean.getBoolean(CLONE_COLLECTION_PROP)));
            }
            return clonedValue;
        }

        @Override
        public void setValue(Object value) {
            resolver.setValue(value);
        }
    }
}
