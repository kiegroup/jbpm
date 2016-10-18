/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration.tools.jpdl;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.jpdl.listeners.TrackingVariableChangeListener;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * JPDL most used assertions.
 */
public class JpdlAssert {

    /**
     * Asserts if the process instance has started.
     *
     * @param pi
     *            process instance
     */
    public static void assertProcessStarted(ProcessInstance pi) {
        Assertions.assertThat(pi).isNotNull();
        Assertions.assertThat(pi.getStart()).isNotNull();
    }

    /**
     * Asserts if the process instance has ended.
     *
     * @param pi
     *            process instance
     */
    public static void assertProcessCompleted(ProcessInstance pi) {
        Assertions.assertThat(pi.hasEnded()).isTrue();
    }

    /**
     * Asserts if the task instance has ended.
     *
     * @param taskName
     *            name of the task
     * @param pi
     *            process instance the task is running in
     */
    public static void assertTaskEnded(String taskName, ProcessInstance pi) {
        TaskInstance ti = JpdlHelper.getTaskInstance(taskName, pi);
        Assertions.assertThat(ti.hasEnded()).isTrue();
    }

    /**
     * Asserts if the task instance has ended.
     *
     * @param ti
     *            task instance
     */
    public static void assertTaskEnded(TaskInstance ti) {
        Assertions.assertThat(ti.hasEnded()).isTrue();
    }

    /**
     * Asserts what was the last change of the given variable
     *
     * @param listener
     *            used instance of TrackingVariableChangeListener
     * @param name
     *            name of the variable
     * @param oldValue
     *            variable's value before the change
     * @param newValue
     *            variable's value after the change
     */
    public static void assertVarLastChange(TrackingVariableChangeListener listener, String name, Object oldValue,
        Object newValue) {
        VariableChange vch = new VariableChange(name, oldValue, newValue);
        VariableChange vchRecord = listener.getLastChange(name);
        Assertions.assertThat(vchRecord).isEqualTo(vch);
    }

    /**
     * Asserts if exists any specified change of the given variable
     *
     * @param listener
     *            used instance of TrackingVariableChangeListener
     * @param name
     *            name of the variable
     * @param oldValue
     *            variable's value before the change
     * @param newValue
     *            variable's value after the change
     */
    public static void assertVarAnyChange(TrackingVariableChangeListener listener, String name, Object oldValue,
        Object newValue) {
        VariableChange vch = new VariableChange(name, oldValue, newValue);
        Assertions.assertThat(listener.getChanges(name).contains(vch)).isTrue();
    }

    /**
     * Asserts if action was called on given node.
     *
     * @param listener
     *            instance of TrackingActionListener registered in action
     *            handler
     * @param name
     *            name of the node
     */
    public static void assertCalledOnNode(TrackingActionListener listener, String name) {
        Assertions.assertThat(listener.wasCalledOnNode(name)).isTrue();
    }

    /**
     * Asserts if action was called on given node <i>n</i> times.
     *
     * @param listener
     *            instance of TrackingActionListener registered in action
     *            handler
     * @param name
     *            name of the node
     * @param n
     *            how many times should have been the action called.
     */
    public static void assertCalledOnNode(TrackingActionListener listener, String name, int n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.format("n must be zero or positive integer, but was: %s", n));
        }
        int i = 0;
        for (Node node : listener.getNodes()) {
            if (node.getName().equals(name)) {
                i++;
            }
        }
        Assertions.assertThat((long) i).isEqualTo((long) n);
    }

    /**
     * Asserts if action was called on given event.
     *
     * @param listener
     *            instance of TrackingActionListener registered in action
     *            handler
     * @param type
     *            type of the event
     */
    public static void assertCalledOnEvent(TrackingActionListener listener, String type) {
        Assertions.assertThat(listener.wasEventAccepted(type)).isTrue();
    }

    /**
     * Asserts if action was called on given event <i>n</i> times.
     *
     * @param listener
     *            instance of TrackingActionListener registered in action
     *            handler
     * @param type
     *            type of the event
     * @param n
     *            how many times should have been the action called.
     */
    public static void assertCalledOnEvent(TrackingActionListener listener, String type, int n) {
        if (n < 0) {
            throw new IllegalArgumentException(String.format("n must be zero or positive integer, but was: %s", n));
        }
        int i = 0;
        for (Event event : listener.getEvents()) {
            if (event.getEventType().equals(type)) {
                i++;
            }
        }
        Assertions.assertThat((long) i).isEqualTo((long) n);
    }
}
