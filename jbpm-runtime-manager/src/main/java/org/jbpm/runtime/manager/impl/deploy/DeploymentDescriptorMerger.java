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

package org.jbpm.runtime.manager.impl.deploy;

import java.util.List;

import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;

public class DeploymentDescriptorMerger {

    public DeploymentDescriptor merge(List<DeploymentDescriptor> descriptorHierarchy, MergeMode mode) {
        return org.kie.internal.runtime.manager.deploy.DeploymentDescriptorMerger.merge(descriptorHierarchy, mode);
    }

    public DeploymentDescriptor merge(DeploymentDescriptor primary, DeploymentDescriptor secondary, MergeMode mode) {
        return org.kie.internal.runtime.manager.deploy.DeploymentDescriptorMerger.merge(primary, secondary, mode);
    }
}
