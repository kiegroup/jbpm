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

package org.jbpm.document.audit;

import org.jbpm.document.Document;
import org.jbpm.document.DocumentCollection;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.internal.process.ProcessVariableIndexer;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DocumentCollectionVariableIndexer implements ProcessVariableIndexer {

    private static final String PATTERN = "{0} ({1}/{2})";

    @Override
    public boolean accept(Object variable) {
        return variable instanceof DocumentCollection;
    }

    @Override
    public List<VariableInstanceLog> index(String name, Object variable) {
        DocumentCollection<? extends Document> documentCollection = (DocumentCollection<? extends Document>) variable;

        int max = documentCollection.getDocuments().size();

        return IntStream.range(0, max)
                .mapToObj(index -> toVariableLog(name, index, max, documentCollection.getDocuments().get(index)))
                .collect(Collectors.toList());
    }

    private VariableInstanceLog toVariableLog(String name, int index, int max, Document document) {

        org.jbpm.process.audit.VariableInstanceLog processVariable = new org.jbpm.process.audit.VariableInstanceLog();
        processVariable.setVariableId(MessageFormat.format(PATTERN, name, index + 1, max));
        processVariable.setValue(document.toString());

        return processVariable;
    }
}
