/**
 * Copyright 2012 JBoss Inc
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
package org.jbpm.integration.console.session;

import java.lang.reflect.Constructor;

import org.drools.KnowledgeBase;

public class SessionManagerFactory {

    @SuppressWarnings("unchecked")
    public static SessionManager getSessionManager(KnowledgeBase kbase) {
        String sessionManager = System.getProperty("jbpm.session.manager");
        if (sessionManager == null) {
            return new MVELSingleSessionManager(kbase);
        }
        
        SessionManager sessionManagerInstance = null;
        try {
            // build session manager based on given class
            Class<SessionManager> sessionManagerClass = (Class<SessionManager>) Class.forName(sessionManager);
            Constructor<SessionManager> c = sessionManagerClass.getConstructor(KnowledgeBase.class);
            sessionManagerInstance = c.newInstance(kbase);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create SessionManager from class " + sessionManager, e);
        }
        
        return sessionManagerInstance;
    }
}
