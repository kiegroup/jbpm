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
package org.jbpm.process.core.context.gateway;

import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.AbstractContext;

public class GatewayScope extends AbstractContext {

    private static final long serialVersionUID = 1L;

    public static final String GATEWAY_SCOPE = "GatewayScope";
    
    private String nodeName;
    private int activeFlows;
    private int mergedFlows;
    
    public String getType() {
        return GATEWAY_SCOPE;
    }

    public Context resolveContext(Object param) {
        if (param.equals(nodeName)) {
            return this;
        }
        
        return null;
    }
    
    public int getActiveFlows() {
        return activeFlows;
    }

    public void setActiveFlows(int activeFlows) {
        this.activeFlows = activeFlows;
    }

    public int getMergedFlows() {
        return mergedFlows;
    }

    public void setMergedFlows(int mergedFlows) {
        this.mergedFlows = mergedFlows;
    }

    public void incrementMerged() {
        this.mergedFlows++;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

}
