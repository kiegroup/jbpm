/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.kie.services.impl.admin;

/*
 * Provides a pre-rendered kie-deployment-descriptor.xml so the test does not depend on JAXB iteration order.
 * Mirrors the descriptor produced by AbstractKieServicesTest#createDeploymentDescriptor() for this fixture,
 * ensuring the deployment remains stable across runs.
 */
final class StableDescriptorXml {

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<deployment-descriptor xsi:schemaLocation=\"http://www.jboss.org/jbpm deployment-descriptor.xsd\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "    <persistence-unit>org.jbpm.domain</persistence-unit>\n"
            + "    <audit-persistence-unit>org.jbpm.domain</audit-persistence-unit>\n"
            + "    <audit-mode>JPA</audit-mode>\n"
            + "    <persistence-mode>JPA</persistence-mode>\n"
            + "    <runtime-strategy>SINGLETON</runtime-strategy>\n"
            + "    <marshalling-strategies/>\n"
            + "    <event-listeners>\n"
            + "        <event-listener>\n"
            + "            <resolver>mvel</resolver>\n"
            + "            <identifier>org.jbpm.kie.test.util.CountDownListenerFactory.get(&quot;processAdminService&quot;, &quot;timer&quot;, 1)</identifier>\n"
            + "            <parameters/>\n"
            + "        </event-listener>\n"
            + "    </event-listeners>\n"
            + "    <task-event-listeners/>\n"
            + "    <globals/>\n"
            + "    <work-item-handlers/>\n"
            + "    <environment-entries/>\n"
            + "    <configurations/>\n"
            + "    <required-roles/>\n"
            + "    <remoteable-classes/>\n"
            + "    <limit-serialization-classes>true</limit-serialization-classes>\n"
            + "</deployment-descriptor>\n";

    private StableDescriptorXml() {
    }

    static String descriptorXml() {
        return XML;
    }
}
