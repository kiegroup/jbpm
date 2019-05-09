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

package org.jbpm.document.marshalling;

import org.jbpm.document.DocumentCollection;
import org.jbpm.document.service.impl.DocumentCollectionImpl;
import org.jbpm.document.service.impl.DocumentImpl;

public class DocumentCollectionImplMarshallingStrategy extends AbstractDocumentCollectionMarshallingStrategy {

	public DocumentCollectionImplMarshallingStrategy(DocumentMarshallingStrategy docMarshallingStrategy) {
		super(docMarshallingStrategy);
	}

	public boolean accept(Object o) {
		return o instanceof DocumentCollectionImpl;
	}

	@Override
	public DocumentCollection<DocumentImpl> buildDocumentCollection() {
		return new DocumentCollectionImpl();
	}
	
}