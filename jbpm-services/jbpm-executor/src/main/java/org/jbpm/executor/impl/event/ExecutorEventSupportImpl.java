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

package org.jbpm.executor.impl.event;

import org.drools.core.event.AbstractEventSupport;
import org.jbpm.executor.AsynchronousJobListener;
import org.kie.api.executor.RequestInfo;


public class ExecutorEventSupportImpl extends AbstractEventSupport<AsynchronousJobListener> implements ExecutorEventSupport {

    @Override
    public void fireBeforeJobScheduled(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.beforeJobScheduled(e) );
        }
    }
    
    @Override
    public void fireBeforeJobExecuted(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.beforeJobExecuted(e) );
        }
    }
    
    @Override
    public void fireBeforeJobCancelled(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.beforeJobCancelled(e) );
        }
    }
    
    @Override
    public void fireAfterJobScheduled(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.afterJobScheduled(e) );
        }
    }
    
    @Override
    public void fireAfterJobExecuted(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.afterJobExecuted(e) );
        }
    }
    
    @Override
    public void fireAfterJobCancelled(final RequestInfo job, Throwable exception) {
        if ( hasListeners() ) {
            notifyAllListeners( new AsynchronousJobEventImpl(job, exception), ( l, e ) -> l.afterJobCancelled(e) );
        }
    }
}
