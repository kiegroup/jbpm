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
package org.jbpm.event.emitters.elasticsearch;

import org.jbpm.persistence.api.integration.InstanceView;

public class DefaultESInstanceViewTransformer implements ESInstanceViewTransformer {
    
    private String index;
    private String type;

    protected DefaultESInstanceViewTransformer (String index, String type) {
        this.index = index;
        this.type = type;
    }
    @Override
    public ESRequest index(InstanceView<?> view) {
        return new ESRequest(index, view.getCompositeId(), type, "index", view);
    }

    @Override
    public ESRequest update(InstanceView<?> view) {
        return new ESRequest(index, view.getCompositeId(), type, "update", view);
    }
}
