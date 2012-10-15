/*
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
package org.jbpm.process.instance.context.gateway;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.core.context.gateway.GatewayScope;
import org.jbpm.process.instance.context.AbstractContextInstance;

public class GatewayScopeInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 1L;
    
    private Map<String, GatewayScope> inclusiveGateways = new HashMap<String, GatewayScope>();

    public String getContextType() {
        
        return GatewayScope.GATEWAY_SCOPE;
    }

    public void addGatewayScope(GatewayScope gatewayScope) {
        if (this.inclusiveGateways.containsKey(gatewayScope.getNodeName())) {
            throw new IllegalStateException("GatewayScope for " + gatewayScope.getNodeName() + " already exists");
        }
        
        this.inclusiveGateways.put(gatewayScope.getNodeName(), gatewayScope);
    }
    
    public GatewayScope getGatewayScope(String nodeName) {
        if (this.inclusiveGateways.containsKey(nodeName)) {
            
            return this.inclusiveGateways.get(nodeName);
        } else if (this.inclusiveGateways.size() == 1) {
            
            return this.inclusiveGateways.values().iterator().next();
        } else {
            throw new IllegalStateException("Cannot find GatewayScope for '" + nodeName + "'");
        }
    }
    
    public Collection<GatewayScope> getGatewayScopes() {
        return this.inclusiveGateways.values();
    }
}
