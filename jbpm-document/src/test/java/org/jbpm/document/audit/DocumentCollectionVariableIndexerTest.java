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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jbpm.document.Documents;
import org.jbpm.document.service.impl.DocumentCollectionImpl;
import org.jbpm.document.service.impl.DocumentImpl;
import org.junit.Test;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocumentCollectionVariableIndexerTest {

    private static final String VARIABLE_NAME = "dox";

    private static final String DOC_1 = "doc_1";
    private static final String DOC_2 = "doc_2";
    private static final String DOC_3 = "doc_3";

    private DocumentCollectionVariableIndexer indexer = new DocumentCollectionVariableIndexer();

    @Test
    public void testAccept() {
        assertFalse(indexer.accept("A String"));
        assertFalse(indexer.accept(57));
        assertFalse(indexer.accept(Boolean.FALSE));
        assertFalse(indexer.accept(null));

        assertTrue(indexer.accept(new Documents()));
    }

    @Test
    public void testIndexEmpty() {
        Assertions.assertThat(indexer.index(VARIABLE_NAME, new Documents()))
                .isEmpty();
    }

    @Test
    public void testIndex() {

        DocumentImpl doc1 = new DocumentImpl(DOC_1, DOC_1, 1024, new Date());
        DocumentImpl doc2 = new DocumentImpl(DOC_2, DOC_3, 1024, new Date());
        DocumentImpl doc3 = new DocumentImpl(DOC_3, DOC_3, 1024, new Date());

        List<VariableInstanceLog> indexed = indexer.index(VARIABLE_NAME, new DocumentCollectionImpl(Arrays.asList(doc1, doc2, doc3)));

        Assertions.assertThat(indexed)
                .hasSize(3);

        testDocument(0, 3, indexed.get(0), doc1);
        testDocument(1, 3, indexed.get(1), doc2);
        testDocument(2, 3, indexed.get(2), doc3);

    }

    private void testDocument(int index, int max, VariableInstanceLog log, DocumentImpl document) {
        Assertions.assertThat(log)
                .hasFieldOrPropertyWithValue("variableId", VARIABLE_NAME + " (" + (index + 1) + "/" + max + ")")
                .hasFieldOrPropertyWithValue("value", document.toString());
    }
}
