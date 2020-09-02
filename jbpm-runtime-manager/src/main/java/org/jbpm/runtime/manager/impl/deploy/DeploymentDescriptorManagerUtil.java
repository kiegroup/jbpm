/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.runtime.manager.impl.deploy;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.compiler.kie.builder.impl.ClasspathKieProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.drools.core.util.IoUtils;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorIO;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeploymentDescriptorManagerUtil {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentDescriptorManagerUtil.class);

    /**
     * This creates a deployment descriptor
     * @param manager
     * @param kieContainer
     * @param presets
     * @return
     */
    public static DeploymentDescriptor getDeploymentDescriptor(DeploymentDescriptorManager manager, KieContainer kieContainer, MergeMode mode, DeploymentDescriptor ...presets) {
        List<DeploymentDescriptor> descriptorHierarchy = new ArrayList<DeploymentDescriptor>();
        

        if(presets != null) {
            descriptorHierarchy.addAll(stream(presets).filter(e -> e != null).collect(toList()));
        }
        KieProject kieProject = ((KieContainerImpl)kieContainer).getKieProject();
        if (kieProject instanceof KieModuleKieProject) {
            InternalKieModule module = ((KieModuleKieProject) kieProject).getInternalKieModule();
            collectDeploymentDescriptors(module, descriptorHierarchy);
        } else if (kieProject instanceof ClasspathKieProject) {
            InputStream is = ((ClasspathKieProject) kieProject).getClassLoader().getResourceAsStream(DeploymentDescriptor.META_INF_LOCATION);
            if(is != null) {
                try {
                    descriptorHierarchy.add(getDescriptorFromContent(IoUtils.readBytesFromInputStream(is)));
                } catch (IOException e) {
                    logger.warn("Could not read deployment descriptor on classpath", e);
                }

            }
        }

        descriptorHierarchy.add(manager.getDefaultDescriptor());
        return new DeploymentDescriptorMerger().merge(descriptorHierarchy, mode);
    }

    public static List<DeploymentDescriptor> getDeploymentDescriptorHierarchy(DeploymentDescriptorManager manager, KieContainer kieContainer) {
        List<DeploymentDescriptor> descriptorHierarchy = new ArrayList<DeploymentDescriptor>();

        KieProject kieProject = ((KieContainerImpl)kieContainer).getKieProject();
        if (kieProject instanceof KieModuleKieProject) {
            InternalKieModule module = ((KieModuleKieProject) kieProject).getInternalKieModule();
            collectDeploymentDescriptors(module, descriptorHierarchy);
        } else if (kieProject instanceof ClasspathKieProject) {
            InputStream is = ((ClasspathKieProject) kieProject).getClassLoader().getResourceAsStream(DeploymentDescriptor.META_INF_LOCATION);
            if(is != null) {
                try {
                    descriptorHierarchy.add(getDescriptorFromContent(IoUtils.readBytesFromInputStream(is)));
                } catch (IOException e) {
                    logger.warn("Could not read deployment descriptor on classpath", e);
                }

            }
        }
        // last is the default descriptor
        descriptorHierarchy.add(manager.getDefaultDescriptor());
        
        return descriptorHierarchy;
    }

    protected static void collectDeploymentDescriptors(InternalKieModule kmodule, List<DeploymentDescriptor> descriptorHierarchy) {
        DeploymentDescriptor descriptor = getDescriptorFromKModule(kmodule);
        if (descriptor != null) {
            descriptorHierarchy.add(descriptor);
        }

        if (kmodule.getKieDependencies() != null) {
            Collection<InternalKieModule> depModules = kmodule.getKieDependencies().values();
            for (InternalKieModule depModule : depModules) {
                collectDeploymentDescriptors(depModule, descriptorHierarchy);
            }
        }
    }

    protected static DeploymentDescriptor getDescriptorFromKModule(InternalKieModule kmodule) {
        DeploymentDescriptor desc = null;
        if (kmodule.isAvailable(DeploymentDescriptor.META_INF_LOCATION)) {
            byte[] content = kmodule.getBytes(DeploymentDescriptor.META_INF_LOCATION);
            desc = getDescriptorFromContent(content);
        }

        return desc;
    }


    protected static DeploymentDescriptor getDescriptorFromContent(byte[] content) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(content)){
            return DeploymentDescriptorIO.fromXml(input);
        } catch (IOException e) {
            logger.warn("Error while reading stream of kie-deployment-descriptor.xml");
            return null;
        }
    }
}
