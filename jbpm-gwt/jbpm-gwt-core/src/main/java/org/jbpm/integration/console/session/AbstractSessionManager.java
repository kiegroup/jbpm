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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractSessionManager implements SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSessionManager.class);
    
    protected int getPersistedSessionId(String location) {
        File sessionIdStore = new File(location + File.separator + "jbpmSessionId.ser");
        if (sessionIdStore.exists()) {
            Integer knownSessionId = null; 
            FileInputStream fis = null;
            ObjectInputStream in = null;
            try {
                fis = new FileInputStream(sessionIdStore);
                in = new ObjectInputStream(fis);
                
                knownSessionId = (Integer) in.readObject();
                
                return knownSessionId.intValue();
                
            } catch (Exception e) {
                return 0;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            
        } else {
            return 0;
        }
    }
    
    protected void persistSessionId(String location, int ksessionId) {
        if (location == null) {
            return;
        }
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(location + File.separator + "jbpmSessionId.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(Integer.valueOf(ksessionId));
            out.close();
        } catch (IOException ex) {
            logger.warn("Error when persisting known session id", ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    protected StatefulKnowledgeSession lookUpInJNDI(String businessKey) {
        try {
            InitialContext ctx = new InitialContext();
        
        
            return (StatefulKnowledgeSession) ctx.lookup(businessKey);
        } catch (NamingException e) {
            logger.warn("Error when looking up session in JNDI", e);
            return null;
        }
    }
    
    protected void removeFromJNDI(String businessKey) {
        try {
            InitialContext ctx = new InitialContext();
            ctx.unbind(businessKey);
        } catch (Exception e) {
            logger.error("Error when removing session from JNDI", e);
        }
    }
    
    protected void bindToJNDI(String businessKey, StatefulKnowledgeSession session) {
        try {
            Context ctx = new InitialContext();
            Name name = ctx.getNameParser("").parse(businessKey);
            
            int size = name.size();
            String atom = name.get(size - 1);
            Context parentCtx = createSubcontext(ctx, name.getPrefix(size - 1));
            parentCtx.bind(atom, session);
            
        } catch (Exception e) {
            logger.error("Error when binding session to JNDI under key " + businessKey, e);
        }
    }
    
    protected Context createSubcontext(Context ctx, Name name)
            throws NamingException {
        Context subctx = ctx;
        for (int pos = 0; pos < name.size(); pos++) {
            String ctxName = name.get(pos);
            try {
                subctx = (Context) ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                subctx = ctx.createSubcontext(ctxName);
            }
            ctx = subctx;
        }
        return subctx;
    }

}
