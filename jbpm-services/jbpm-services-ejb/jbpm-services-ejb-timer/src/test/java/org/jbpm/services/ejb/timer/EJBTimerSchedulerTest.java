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
import org.jbpm.persistence.timer.GlobalJpaTimerJobInstance;
import org.junit.Before;
import org.junit.Test;


public class EJBTimerSchedulerTest {

    private Collection<Timer> timers = new ArrayList<>();
    private Timer timer1 = mock(Timer.class);
    private EjbGlobalJobHandle ejbGlobalJobHandle1;
    private EJBTimerScheduler scheduler;
    
    @Before
    public void setup() {
        TimerService timerService = mock(TimerService.class);
        when(timerService.getTimers()).thenReturn(timers);
        
        GlobalJpaTimerJobInstance timerJobInstance1 = mock(GlobalJpaTimerJobInstance.class);
        ejbGlobalJobHandle1 = new EjbGlobalJobHandle(1L, "test job", "test");
        ejbGlobalJobHandle1.setTimerJobInstance(timerJobInstance1);
        when(timerJobInstance1.getJobHandle()).thenReturn(ejbGlobalJobHandle1);
        
        when(timer1.getInfo()).thenReturn(new EjbTimerJob(timerJobInstance1));
        
        timers.add(timer1);
        
        scheduler = new EJBTimerScheduler();
        scheduler.setUseLocalCache(true);
        scheduler.timerService = timerService;
    }
    
    @Test
    public void testEjbTimerSchedulerTestOnTimerLoop() {
        // first call to go over list of timers should not add anything to the cache as there is no matching timers
        TimerJobInstance jobInstance = scheduler.getTimerByName("not existing");
        assertNull(jobInstance);
        // second call should result in exact same behavior
        jobInstance = scheduler.getTimerByName("not existing");
        assertNull(jobInstance);
        // calling for existing timer should return it as it matches and thus also add it to cache
        jobInstance = scheduler.getTimerByName("test job");
        assertNotNull(jobInstance);
        
        //Remove job from cache and quit the timer from the getTimers mock call
        scheduler.removeJob(ejbGlobalJobHandle1, (Timer) null);
        timers.remove(timer1);
        jobInstance = scheduler.getTimerByName("test job");
        assertNull(jobInstance);
    }

    @Test
    public void evictCacheForEjbTimerSchedulerTest() {
        // calling for existing timer should return it as it matches and thus also add it to cache
        TimerJobInstance jobInstance = scheduler.getTimerByName("test job");
        assertNotNull(jobInstance);
        
        //Timer is removed but still found in cache
        timers.remove(timer1);
        jobInstance = scheduler.getTimerByName("test job");
        assertNotNull(jobInstance);
        
        //After evicting cache, timer is completely removed
        scheduler.evictCache(ejbGlobalJobHandle1);
        jobInstance = scheduler.getTimerByName("test job");
        assertNull(jobInstance);
    }
}