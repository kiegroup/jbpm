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

package org.jbpm.document.marshalling;

import org.jbpm.document.Document;
import org.jbpm.document.DocumentCollection;
import org.jbpm.document.Documents;
import org.jbpm.document.service.impl.DocumentCollectionImpl;
import org.kie.api.marshalling.ObjectMarshallingStrategy;

/**
 * {@link ObjectMarshallingStrategy} for a collection (List) of {@link Document Documents}.
 * 
 * 
 * * @deprecated This class is deprecated because the deprecation of {@link Documents} class. Please use the
 * {@link DocumentCollectionImplMarshallingStrategy} to marshal and unmarshal a collection of {@link DocumentCollectionImpl}.
 */
@Deprecated
public class DocumentsMarshallingStrategy extends AbstractDocumentCollectionMarshallingStrategy {

    public DocumentsMarshallingStrategy(DocumentMarshallingStrategy docMarshallingStrategy) {
        super(docMarshallingStrategy);
    }

    public boolean accept(Object o) {
        return o instanceof Documents;
    }

    @Override
    public DocumentCollection<Document> buildDocumentCollection() {
        return new Documents();
    }

}
