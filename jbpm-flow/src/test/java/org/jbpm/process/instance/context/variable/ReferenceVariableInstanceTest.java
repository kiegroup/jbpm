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

package org.jbpm.process.instance.context.variable;

import org.jbpm.process.core.context.variable.ReferenceVariableInstance;
import org.jbpm.process.core.context.variable.Variable;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceVariableInstanceTest {

    @Test
    public void shouldNotSetNullTwice() {
        Handler handler = new Handler();
        ReferenceVariableInstance<String> v =
                new ReferenceVariableInstance<>(
                        new Variable("my-var"), handler);

        // set var 3 times
        v.set("test");
        // but nulls count for 1
        v.set(null);
        v.set(null);

        assertThat(handler.hits)
                .as("my-var should be set only 2 times")
                .isEqualTo(2);
    }

    static class Handler implements ReferenceVariableInstance.OnSetHandler<String> {

        public int hits = 0;

        @Override
        public void before(String oldValue, String newValue) {
            hits++;
        }

        @Override
        public void after(String oldValue, String newValue) {

        }
    }
}