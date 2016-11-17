/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.runtime.manager.impl.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;

/**
 * SessionFactory implementation backed with an in-memory store of used sessions. This does not preserve state
 * between server restarts or even <code>RuntimeManager</code> close. For that, the more permanent store 
 * <code>JPASessionFactory</code> should be used.
 *
 * @see JPASessionFactory
 */
public class InMemorySessionFactory implements SessionFactory {

    private RuntimeEnvironment environment;
    private KieBase kbase;
    // TODO all sessions stored here should be proxied so it can be removed on dispose/destroy
    private Map<Long, KieSession> sessions = new ConcurrentHashMap<Long, KieSession>();
    
    public InMemorySessionFactory(RuntimeEnvironment environment) {
        this.environment = environment;
        this.kbase = environment.getKieBase();
    }
    
    private KieSession wrapSession(KieSession original) {
        KieSessionProxy proxy = new KieSessionProxy(original);

        return (KieSession) Proxy.newProxyInstance(
                KieSession.class.getClassLoader(),
                new Class[]{ KieSession.class},
                proxy);
    }

    @Override
    public KieSession newKieSession() {
        KieSession ksession = kbase.newKieSession(environment.getConfiguration(), environment.getEnvironment());
        this.sessions.put(ksession.getIdentifier(), ksession);
        return wrapSession(ksession);
    }

    @Override
    public KieSession findKieSessionById(Long sessionId) {
        if (sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        } else {
            throw new SessionNotFoundException("Session with id " + sessionId + " was not found");
        }
    }

    @Override
    public void close() {
        sessions.clear();
    }

    private class KieSessionProxy implements InvocationHandler {
        private KieSession original;

        KieSessionProxy(KieSession orig) {
            original = orig;
        }

        /**
         * Processes a method invocation on a proxy instance and returns
         * the result.  This method will be invoked on an invocation handler
         * when a method is invoked on a proxy instance that it is
         * associated with.
         *
         * @param proxy  the proxy instance that the method was invoked on
         * @param method the {@code Method} instance corresponding to
         *               the interface method invoked on the proxy instance.  The declaring
         *               class of the {@code Method} object will be the interface that
         *               the method was declared in, which may be a superinterface of the
         *               proxy interface that the proxy class inherits the method through.
         * @param args   an array of objects containing the values of the
         *               arguments passed in the method invocation on the proxy instance,
         *               or {@code null} if interface method takes no arguments.
         *               Arguments of primitive types are wrapped in instances of the
         *               appropriate primitive wrapper class, such as
         *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
         * @return the value to return from the method invocation on the
         * proxy instance.  If the declared return type of the interface
         * method is a primitive type, then the value returned by
         * this method must be an instance of the corresponding primitive
         * wrapper class; otherwise, it must be a type assignable to the
         * declared return type.  If the value returned by this method is
         * {@code null} and the interface method's return type is
         * primitive, then a {@code NullPointerException} will be
         * thrown by the method invocation on the proxy instance.  If the
         * value returned by this method is otherwise not compatible with
         * the interface method's declared return type as described above,
         * a {@code ClassCastException} will be thrown by the method
         * invocation on the proxy instance.
         * @throws Throwable the exception to throw from the method
         *                   invocation on the proxy instance.  The exception's type must be
         *                   assignable either to any of the exception types declared in the
         *                   {@code throws} clause of the interface method or to the
         *                   unchecked exception types {@code java.lang.RuntimeException}
         *                   or {@code java.lang.Error}.  If a checked exception is
         *                   thrown by this method that is not assignable to any of the
         *                   exception types declared in the {@code throws} clause of
         *                   the interface method, then an
         *                   {@ link UndeclaredThrowableException} containing the
         *                   exception that was thrown by this method will be thrown by the
         *                   method invocation on the proxy instance.
         * @ see UndeclaredThrowableException
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equalsIgnoreCase("dispose") || method.getName().equalsIgnoreCase("destroy")) {
                sessions.remove(original.getIdentifier());
            }

            return method.invoke(original, args);
        }
    }
}
