package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.runtime.process.ProcessInstance;

public class TrackingProcessEventListener extends DefaultProcessEventListener {
    private final List<String> processesStarted = new ArrayList<String>();
    private final List<String> processesCompleted = new ArrayList<String>();
    private final List<String> processesAborted = new ArrayList<String>();

    private final List<String> nodesTriggered = new ArrayList<String>();
    private final List<String> nodesLeft = new ArrayList<String>();

    private final List<String> variablesChanged = new ArrayList<String>();

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        super.afterNodeTriggered(event);

        nodesTriggered.add(event.getNodeInstance().getNodeName());
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        super.afterNodeLeft(event);

        nodesLeft.add(event.getNodeInstance().getNodeName());
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        processesStarted.add(event.getProcessInstance().getProcessId());
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        if (event.getProcessInstance().getState() == ProcessInstance.STATE_ABORTED) {
            processesAborted.add(event.getProcessInstance().getProcessId());
        } else {
            processesCompleted.add(event.getProcessInstance().getProcessId());
        }
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        variablesChanged.add(event.getVariableId());
    }

    public List<String> getNodesTriggered() {
        return Collections.unmodifiableList(nodesTriggered);
    }

    public List<String> getNodesLeft() {
        return Collections.unmodifiableList(nodesLeft);
    }

    public List<String> getProcessesStarted() {
        return Collections.unmodifiableList(processesStarted);
    }

    public List<String> getProcessesCompleted() {
        return Collections.unmodifiableList(processesCompleted);
    }
    
    public List<String> getProcessesAborted() {
        return Collections.unmodifiableList(processesAborted);
    }

    public List<String> getVariablesChanged() {
        return Collections.unmodifiableList(variablesChanged);
    }

    public boolean wasNodeTriggered(String nodeName) {
        return nodesTriggered.contains(nodeName);
    }

    public boolean wasNodeLeft(String nodeName) {
        return nodesLeft.contains(nodeName);
    }

    public boolean wasProcessStarted(String processName) {
        return processesStarted.contains(processName);
    }

    public boolean wasProcessCompleted(String processName) {
        return processesCompleted.contains(processName);
    }
    
    public boolean wasProcessAborted(String processName) {
        return processesAborted.contains(processName);
    }

    public boolean wasVariableChanged(String variableId) {
        return variablesChanged.contains(variableId);
    }

    public void clear() {
        nodesTriggered.clear();
        nodesLeft.clear();
        processesStarted.clear();
        processesCompleted.clear();
        processesAborted.clear();
        variablesChanged.clear();
    }
}
