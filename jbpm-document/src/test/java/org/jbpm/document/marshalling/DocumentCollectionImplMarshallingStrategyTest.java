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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbpm.document.Document;
import org.jbpm.document.DocumentCollection;
import org.jbpm.document.service.impl.DocumentCollectionImpl;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.document.service.impl.DocumentStorageServiceImpl;
import org.junit.Test;

public class DocumentCollectionImplMarshallingStrategyTest {

	private static final String STORAGE_PATH_TEST = "target/docs";
	
	private DocumentCollectionImplMarshallingStrategy docsMarshallingStrategy = new DocumentCollectionImplMarshallingStrategy(createSingleDocMarshallingStrategy());
	
	@Test
	public void testMarshallUnmarshall() throws IOException, ClassNotFoundException {
		
		List<DocumentImpl> documents = getDocuments();
		DocumentCollectionImpl docs = new DocumentCollectionImpl(documents);
		
		byte[] marshalledDocs = docsMarshallingStrategy.marshal(null, null, docs);
		
		DocumentCollectionImpl unmarshalledDocs = (DocumentCollectionImpl) docsMarshallingStrategy.unmarshal(null, null, marshalledDocs, this.getClass().getClassLoader());
		
		assertEquals(docs.getDocuments().size(), unmarshalledDocs.getDocuments().size());
		
		List<DocumentImpl> unmarshalledDocumentsList = unmarshalledDocs.getDocuments();
		
		assertEquals(documents.size(), unmarshalledDocumentsList.size());
		
		assertEquals(unmarshalledDocumentsList.get(0).getName(), docs.getDocuments().get(0).getName());
		assertEquals(unmarshalledDocumentsList.get(0).getLink(), docs.getDocuments().get(0).getLink());
		assertEquals(unmarshalledDocumentsList.get(1).getName(), docs.getDocuments().get(1).getName());
		assertEquals(unmarshalledDocumentsList.get(1).getLink(), docs.getDocuments().get(1).getLink());
	}
	
	private DocumentImpl getDocument(String documentName) {
		DocumentImpl documentOne = new DocumentImpl();
		documentOne.setIdentifier(documentName);
		documentOne.setLastModified(new Date());
		documentOne.setLink("http://" +  documentName);
		documentOne.setName(documentName + " Name");
		documentOne.setSize(1);
		documentOne.setContent(documentName.getBytes());
		return documentOne;
	}
	
	private List<DocumentImpl> getDocuments() {
		
		List<DocumentImpl> documents = new ArrayList<>();
		
		documents.add(getDocument("documentOne"));
		documents.add(getDocument("documentTwo"));
		
		return documents;
	}
	
	private DocumentMarshallingStrategy createSingleDocMarshallingStrategy() {
        return new DocumentMarshallingStrategy(new DocumentStorageServiceImpl(STORAGE_PATH_TEST));
    }

}
