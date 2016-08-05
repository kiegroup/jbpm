package org.jbpm.workflow.instance.impl.queue;

import org.jbpm.process.instance.ProcessImplementationPart;
import org.jbpm.process.instance.context.exception.CompensationScopeInstance;

public class AfterCompensationAction implements ProcessInstanceAction {

    private final CompensationScopeInstance compensationScopeInst;

    public AfterCompensationAction( CompensationScopeInstance compensationScopeInstance ) {
        this.compensationScopeInst = compensationScopeInstance;
    }

    @Override
    public void trigger() {
        this.compensationScopeInst.afterCompensation();
    }

    @Override
    public boolean actsOn(ProcessImplementationPart instance) {
        if( instance instanceof CompensationScopeInstance ) {
           return ( this.compensationScopeInst.getContextId() == ((CompensationScopeInstance) instance).getContextId());
        }
        return false;
    }


    @Override
    public String toString() {
        return CompensationScopeInstance.class.getSimpleName() + ".afterCompensation()";
    }
}
