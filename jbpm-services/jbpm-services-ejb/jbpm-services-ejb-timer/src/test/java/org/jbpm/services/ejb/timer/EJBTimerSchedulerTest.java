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
package org.jbpm.services.ejb.timer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.drools.core.time.impl.TimerJobInstance;
import org.junit.Test;


public class EJBTimerSchedulerTest {

    @Test
    public void testEjbTimerSchedulerTestOnTimerLoop() {
        
        Collection<Timer> timers = new ArrayList<>();
        
        TimerService timerService = mock(TimerService.class);
        when(timerService.getTimers()).thenReturn(timers);
        
        TimerJobInstance timerJobInstance1 = mock(TimerJobInstance.class);
        EjbGlobalJobHandle ejbGlobalJobHandle1 = new EjbGlobalJobHandle(1L, "test job", "test");
        ejbGlobalJobHandle1.setTimerJobInstance(timerJobInstance1);
        when(timerJobInstance1.getJobHandle()).thenReturn(ejbGlobalJobHandle1);
        
        Timer timer1 = mock(Timer.class);
        when(timer1.getInfo()).thenReturn(new EjbTimerJob(timerJobInstance1));
        
        timers.add(timer1);
        
        EJBTimerScheduler scheduler = new EJBTimerScheduler();
        scheduler.timerService = timerService;
        // first call to go over list of timers should not add anything to the cache as there is no matching timers
        TimerJobInstance jobInstance = scheduler.getTimerByName("not existing");
        assertNull(jobInstance);
        // second call should result in exact same behavior
        jobInstance = scheduler.getTimerByName("not existing");
        assertNull(jobInstance);
        // calling for existing timer should return it as it matches and thus also add it to cache
        jobInstance = scheduler.getTimerByName("test job");
        assertNotNull(jobInstance);
    }

}
