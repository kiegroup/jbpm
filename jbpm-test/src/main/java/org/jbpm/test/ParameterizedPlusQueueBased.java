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
package org.jbpm.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jbpm.test.ParameterizedPlusQueueBased.ExecutionType;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.parameterized.ParametersRunnerFactory;


/**
 * This {@link Suite} allows us to add a recursive vs. queue-based execution model dimension to parameters
 * <em>without</em> having to add that logic to the {@link Parameters} annotated method of a test!
 * </p>
 * The only additional requirement is that the constructor of the test is modified to include a last argument of
 * the type {@link ExecutionType} (defined in this class).
 * </p>
 * This constructor should then also contain the following line:
 * <pre>
 * this.queueBasedExecution = (executionType.equals(ExecutionType.QUEUE_BASED));
 * </pre>
 * where <code>executionType</code> is the aforementioned last argument to the constructor.
 * </p>
 * The <code>queueBasedExecution</code> field is a protected field of the {@link JbpmJUnitBaseTestCase}.
 */
public class ParameterizedPlusQueueBased extends Suite {

    private final List<Runner> runners;

    private final Parameterized _parameterizedDelegate;

    public static enum ExecutionType {
        QUEUE_BASED("QUEUE BASED"),
        RECURSIVE("RECURSIVE");

        private final String name;

        private ExecutionType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public ParameterizedPlusQueueBased(Class<?> klass) throws Throwable {
        // Give an empty list of Runner's since we're overriding the getChildren() method anyway!
        super(klass, Collections.emptyList());
        _parameterizedDelegate = new Parameterized(klass);

        ParametersRunnerFactory runnerFactory = callParameterizedMethod(
                "getParametersRunnerFactory",
                new Object [] { klass },
                new Class[] { Class.class });
        Parameters parameters = ((FrameworkMethod) callParameterizedMethod(
                "getParametersMethod",
                new Object[0], new Class[0])).getAnnotation(Parameters.class);
        Iterable<Object> allParameters = callParameterizedMethod("allParameters", new Object[0], new Class[0]);

        // add recursive/queue-based parameter dimension
        allParameters = addQueueBasedParameterToParameters(allParameters);

        int numParams = ((Object[]) allParameters.iterator().next()).length;
        String nameWithQueueBased = parameters.name() + ", {" + (numParams-1) + "}";
        List<Runner> runnersList = callParameterizedMethod(
                "createRunnersForParameters",
                new Object[] { allParameters, nameWithQueueBased, runnerFactory },
                new Class[]  { Iterable.class, String.class, ParametersRunnerFactory.class} );

        runners = Collections.unmodifiableList(runnersList);
    }

    /**
     * @param allParameters
     * @return
     */
    private Iterable<Object> addQueueBasedParameterToParameters(Iterable<Object> allParameters) {
        List<Object> newAllParameters = new ArrayList<Object>();
        for( Object paramObj : allParameters ) {
           Object[] params = (Object[]) paramObj;
           for( int i = 0; i < 2; ++i ) {
               ExecutionType queueBasedOrRecursive = (i%2==0) ? ExecutionType.RECURSIVE : ExecutionType.QUEUE_BASED;
               newAllParameters.add(new Object[] { params[0], params[1], queueBasedOrRecursive });
           }
        }
        return newAllParameters;
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    private <T> T callParameterizedMethod(String methodName, Object [] args, Class [] paramTypes) {
        Method method;
        try {
            method = Parameterized.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Unable to retrieve method [" + Parameterized.class.getSimpleName() + "." + methodName + "]");
        }
        try {
            return (T) method.invoke(_parameterizedDelegate, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Unable to invoke method [" + Parameterized.class.getSimpleName() + "." + methodName + "]");
        }
    }
}
