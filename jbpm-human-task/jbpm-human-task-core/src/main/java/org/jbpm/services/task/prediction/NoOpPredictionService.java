/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task.prediction;

import java.util.Map;

import org.kie.api.task.model.Task;
import org.kie.internal.task.api.prediction.PredictionOutcome;
import org.kie.internal.task.api.prediction.PredictionService;

/**
 * Implementation of a no-op prediction service
 */
public class NoOpPredictionService implements PredictionService {

    public static final String IDENTIFIER = "NoOp";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public PredictionOutcome predict(Task task, Map<String, Object> inputData) {
        // returns empty (not present) prediction outcome
        return new PredictionOutcome();
    }

    @Override
    public void train(Task task, Map<String, Object> inputData, Map<String, Object> outputData) {
        // do nothing, no model to train
    }

}
