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
package org.jbpm.test.functional.workitem;

import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.ProcessWorkItemHandlerException;

public class ExceptionWorkItemHandler extends AbstractLogOrThrowWorkItemHandler {

    public ExceptionWorkItemHandler() {
        this.handlingProcessId = null;
        this.handlingStrategy = null;
    }

    public ExceptionWorkItemHandler(String handlingProcessId,
                                     String handlingStrategy) {
        this.handlingProcessId = handlingProcessId;
        this.handlingStrategy = handlingStrategy;
    }

    @Override
    public void executeWorkItem( WorkItem workItem, WorkItemManager manager ) {
        Object exception = workItem.getParameter( "exception" );
        if (handlingProcessId == null && handlingStrategy == null) {
            try {
                if ( exception != null && "no".equals( (String)exception ) ) {
                    manager.completeWorkItem( workItem.getId(), null );
                }
                else {
                    throwExceptionSoThatWorkItemIsNOTCompleted( workItem );
                }
            } catch ( RuntimeException e ) {
                e.printStackTrace();
            }
        } else {
            throw new ProcessWorkItemHandlerException(handlingProcessId, handlingStrategy, new RuntimeException("On purpose"));
        }
    }

    @Override
    public void abortWorkItem( WorkItem workItem, WorkItemManager manager ) {
        // TODO Auto-generated method stub

    }

    private void throwExceptionSoThatWorkItemIsNOTCompleted( WorkItem workItem ) {
        throw new RuntimeException( "Did not complete work item " + workItem.getName() + "/" + workItem.getId()
                + " from node " + ((org.drools.core.process.instance.WorkItem) workItem).getNodeId() );
    }

}
