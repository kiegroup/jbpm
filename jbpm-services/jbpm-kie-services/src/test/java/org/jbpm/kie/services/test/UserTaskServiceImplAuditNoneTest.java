/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.kie.services.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import static org.kie.internal.runtime.conf.AuditMode.NONE;

public class UserTaskServiceImplAuditNoneTest extends UserTaskServiceImplTest {

    @Override
    protected InternalKieModule createKJAR(KieServices ks, ReleaseId releaseId, List<String> processes) {
        DeploymentDescriptor customDescriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
        customDescriptor.getBuilder().auditMode(NONE);
       
        Map<String, String> resources = new HashMap<String, String>();
        resources.put("src/main/resources/" + DeploymentDescriptor.META_INF_LOCATION, customDescriptor.toXml());
        
        return createKieJar(ks, releaseId, processes, resources);
    }
    
    @Override
    protected boolean isJPAAuditMode() {
        return false;
    }
}
