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

    private static final String AUDIT_DISABLED_DESCRIPTOR =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<deployment-descriptor xsi:schemaLocation=\"http://www.jboss.org/jbpm deployment-descriptor.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "    <persistence-unit>org.jbpm.domain</persistence-unit>\n" +
            "    <audit-persistence-unit>org.jbpm.domain</audit-persistence-unit>\n" +
            "    <audit-mode>NONE</audit-mode>\n" +
            "    <persistence-mode>JPA</persistence-mode>\n" +
            "    <runtime-strategy>SINGLETON</runtime-strategy>\n" +
            "    <marshalling-strategies/>\n" +
            "    <event-listeners/>\n" +
            "    <task-event-listeners/>\n" +
            "    <globals/>\n" +
            "    <work-item-handlers/>\n" +
            "    <environment-entries/>\n" +
            "    <configurations/>\n" +
            "    <required-roles/>\n" +
            "    <remoteable-classes/>\n" +
            "    <limit-serialization-classes>true</limit-serialization-classes>\n" +
            "</deployment-descriptor>\n";

    @Override
    protected InternalKieModule createKJAR(KieServices ks, ReleaseId releaseId, List<String> processes) {
        Map<String, String> resources = new HashMap<String, String>();
        resources.put("src/main/resources/" + DeploymentDescriptor.META_INF_LOCATION, AUDIT_DISABLED_DESCRIPTOR);

        return createKieJar(ks, releaseId, processes, resources);
    }
    
    @Override
    protected boolean isJPAAuditMode() {
        return false;
    }
}
