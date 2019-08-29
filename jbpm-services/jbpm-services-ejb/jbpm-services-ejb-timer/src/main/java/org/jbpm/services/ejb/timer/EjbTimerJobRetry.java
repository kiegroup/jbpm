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

package org.jbpm.services.ejb.timer;

import org.drools.core.time.impl.TimerJobInstance;

public class EjbTimerJobRetry extends EjbTimerJob {

    private static final long serialVersionUID = 4560504301479269768L;
    private int retry;

    public EjbTimerJobRetry(TimerJobInstance timerJobInstance, int retry) {
        super(timerJobInstance);
        this.retry = retry;
    }

    public EjbTimerJobRetry(TimerJobInstance instance) {
        this(instance, 1);
    }

    public int getRetry() {
        return retry;
    }

    public EjbTimerJobRetry next() {
        return new EjbTimerJobRetry(getTimerJobInstance(), getRetry() + 1);
    }

}
