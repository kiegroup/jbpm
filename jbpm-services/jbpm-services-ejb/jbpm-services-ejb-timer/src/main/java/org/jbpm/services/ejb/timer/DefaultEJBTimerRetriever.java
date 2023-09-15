/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ejb.Timer;
import javax.ejb.TimerService;

class DefaultEJBTimerRetriever extends EJBTimerRetriever<Void> {
    
    protected DefaultEJBTimerRetriever(TimerService timerService) {
        super(timerService);
    }

    @Override
    public Collection<Object> getTimers(String jobName, Void accepted) {
        return timerService.getTimers().stream().map(Timer::getInfo).collect(Collectors.toList());
    }
}
