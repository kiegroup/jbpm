/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.runtime.manager.impl.error;

import java.util.concurrent.atomic.AtomicInteger;

import org.kie.internal.runtime.error.ExecutionError;
import org.kie.internal.runtime.error.ExecutionErrorListener;

public class CountExecutionErrorListener implements ExecutionErrorListener {

    private static AtomicInteger count = new AtomicInteger(0);
    
    @Override
    public void onExecutionError(ExecutionError error) {
        count.incrementAndGet();
    }

    public static Integer getCount() {
        return count.get();
    }
    
    public static void reset(){
        count.set(0);
    }
}
