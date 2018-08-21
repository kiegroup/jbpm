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

package org.jbpm.process.core.context.variable;

import java.io.Serializable;

import org.jbpm.process.instance.context.variable.VariableScopeInstance;

public class ReferenceVariableInstance<T> implements VariableInstance<T>, Serializable {

    transient private final Variable variableDescriptor;
    transient private final VariableScopeInstance parentScopeInstance;
    transient private final OnSetHandler<T> onSet;
    private ValueReference<T> delegate;

    public ReferenceVariableInstance(VariableScopeInstance parentScopeInstance, Variable variableDescriptor, OnSetHandler<T> handler) {
        this.parentScopeInstance = parentScopeInstance;
        this.variableDescriptor = variableDescriptor;
        this.onSet = handler;
        this.delegate = new SimpleValueReference<>(null);
        T value = (T) variableDescriptor.getValue();
        if (value != null) set(value);
    }

    public String name() {
        return variableDescriptor.getName();
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public void set(T value) {
        T oldValue = delegate.get();
        onSet.before(oldValue, value);
        delegate.set(value);
        onSet.after(oldValue, value);
    }

    public void setReference(ValueReference<T> delegate) {
        T oldValue = this.delegate.get();
        T value = delegate.get();
        onSet.before(oldValue, value);
        this.delegate = delegate;
        onSet.after(oldValue, value);
    }

    public ValueReference<T> getReference() {
        return delegate;
    }

    public interface OnSetHandler<T> extends Serializable {
        static <T> Empty<T> empty() { return Empty.instance; }
        class Empty<T> implements OnSetHandler<T> {
            static Empty instance = new Empty();

            @Override
            public void before(T oldValue, T newValue) {

            }

            @Override
            public void after(T oldValue, T newValue) {

            }
        }
        void before(T oldValue, T newValue);
        void after(T oldValue, T newValue);
    }
}
