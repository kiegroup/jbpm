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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.internal.task.api.prediction.PredictionService;

public class PredictionServiceRegistry {

    private static final ServiceLoader<PredictionService> foundServices = ServiceLoader.load(PredictionService.class, PredictionServiceRegistry.class.getClassLoader());
    private static final String selectedService = System.getProperty("org.jbpm.task.prediction.service", NoOpPredictionService.IDENTIFIER);
    private static final Map<String, PredictionService> predictionServices = new HashMap<>();

    private PredictionServiceRegistry() {

        foundServices
                .forEach(strategy -> predictionServices.put(strategy.getIdentifier(), strategy));
    }

    public static PredictionServiceRegistry get() {
        return Holder.INSTANCE;
    }

    public PredictionService getService() {
        PredictionService predictionService = predictionServices.get(selectedService);
        if (predictionService == null) {
            throw new IllegalArgumentException("No prediction service was found with id " + selectedService);
        }

        return predictionService;
    }

    private static class Holder {
        static final PredictionServiceRegistry INSTANCE = new PredictionServiceRegistry();
    }

    public synchronized void addStrategy(PredictionService predictionService) {
        predictionServices.put(predictionService.getIdentifier(), predictionService);

    }
}
