/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.audit.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TrackingIndexWriter;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.jbpm.services.task.audit.query.QueryResult;

public class LuceneIndexService implements IndexService {

    public static final String BINARY = "binary";

    private Map<Class, ModelIndex> models = new HashMap<Class, ModelIndex>();

    private Analyzer keywordAnalyzer = new KeywordAnalyzer();

    private IndexWriter iw;
    private LuceneQueryBuilder queryBuilder;
    private final StatusIndex statusIndex = new StatusIndex();
    private TrackingIndexWriter tiw;
    private SearcherManager sm;
    private ControlledRealTimeReopenThread<IndexSearcher> reopener;

    private ThreadLocal<List<Document>> adds =
        new ThreadLocal<List<Document>>() {
            @Override
            protected List<Document> initialValue() {
                return new ArrayList<Document>();
            }
        };

    private ThreadLocal<List<Document>> updates =
        new ThreadLocal<List<Document>>() {
            @Override
            protected List<Document> initialValue() {
                return new ArrayList<Document>();
            }
        };

    private ThreadLocal<List<String>> deletes =
        new ThreadLocal<List<String>>() {
            @Override
            protected List<String> initialValue() {
                return new ArrayList<String>();
            }
        };


    public LuceneIndexService() {
        Directory directory = new RAMDirectory();
        queryBuilder = new LuceneQueryBuilder(statusIndex);
        try {
            iw = new IndexWriter(directory,
                new IndexWriterConfig(Version.LUCENE_47, keywordAnalyzer));
            sm = new SearcherManager(iw, true, new WarmSearchFactory());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        tiw = new TrackingIndexWriter(iw);
        reopener = new ControlledRealTimeReopenThread(tiw, sm, 3, 0);
        reopener.setDaemon(true);
        reopener.start();
    }

    public ModelIndex addModel(ModelIndex index) {
        return models.put(index.getModelInterface(), index);
    }

    @Override
    public void prepare(Collection updates, Collection inserts,
        Collection deletes) throws IOException {
        this.adds.get().clear();
        this.updates.get().clear();
        this.deletes.get().clear();
        if (inserts != null) {
            for (Object t : inserts) {
                tiw.addDocument(prepareDocument(t));
            }
        }
        if (updates != null) {
        for (Object t : updates) {
            tiw.updateDocument(
                new Term("id", String.valueOf(getModel(t.getClass()).getId(t))),
                prepareDocument(t));
        }
        }
        if (deletes != null) {
            for (Object t : deletes) {
                tiw.deleteDocuments(
                    new Term("id", String.valueOf(getModel(t.getClass()).getId(t))));
        }
        }
        //tiw.getIndexWriter().prepareCommit();
    }

    @Override
    public void commit() throws IOException {
        //tiw.getIndexWriter().commit();
    }

    @Override
    public void rollback() {
        try {
            tiw.getIndexWriter().rollback();
        } catch (IOException e) {
            throw new RuntimeException("Unable to rollback", e);
        }
    }

    @Override
    public void buildIndex(Iterator previousTasks) throws IOException {
        while (previousTasks.hasNext()) {
            iw.addDocument(prepareDocument(previousTasks.next()));
        }
    }

    @Override
    public <T> QueryResult<T> find(int offset, int count,
        QueryComparator<T> comparator, Class<T> clazz, Filter<?, ?>... filters)
        throws IOException {
        ModelIndex<T> index = getModel(clazz);
        if (index == null) {
            throw new IllegalArgumentException(
                clazz.getName() + " no index defined");
        }
        if (filters != null) {
            Filter[] tmp = new Filter[filters.length + 1];
            tmp[0] = index.getTypeFilter();
            System.arraycopy(filters, 0, tmp, 1, filters.length);
            filters = tmp;
        } else {
            filters = new Filter[]{index.getTypeFilter()};
        }
        //TODO - here we always wait for newest changes - how to determine if that is needed
        IndexSearcher search = getSearcher(tiw.getGeneration());
        Query query = queryBuilder.buildQuery(search, filters);
        TopDocs td;
        count = count == 0 ? Integer.MAX_VALUE : count;
        int number =  1l*offset + count <= Integer.MAX_VALUE ? offset + count : Integer.MAX_VALUE;

        if (comparator != null) {
            td = search.search(query, number, queryBuilder.getSort(comparator));
        }  else {
            td = search.search(query, number);
        }
        int c = 0;
        List<T> l = new ArrayList<T>();
        try {
            while (c < count && offset + c < td.totalHits) {
                Document doc = search.doc(td.scoreDocs[offset + c++].doc);
                l.add(index.fromBytes(doc.getBinaryValue(BINARY).bytes));
            }
        } finally {
            sm.release(search);
        }
        return new QueryResult<T>(offset, td.totalHits, l);
    }

    private ModelIndex getModel(Class obj) {
        ModelIndex mi = models.get(obj);
        synchronized (this) {
            if (mi == null) {
                for (ModelIndex index : models.values()) {
                    if (index.isModelFor(obj)) {
                        mi = index;
                        break;
                    }
                }
                if (mi != null) {
                    models.put(obj, mi);
                }
            }
            if (mi == null) {
                throw new IllegalStateException("No index model is configured for " + obj.getCanonicalName());
            }
            return mi;
        }
    }

    private Document prepareDocument(Object t) {
        ModelIndex index = getModel(t.getClass());
        if (index == null) {
            throw new IllegalArgumentException(
                t.getClass().getName() + " no index defined");
        }
        Document isPrepared =  index.prepare(t);
        if (isPrepared.getField("status") != null) {
            IndexableField taskId = isPrepared.getField("taskId");
            IndexableField fi = isPrepared.getField("status");
            if (taskId != null) {
                statusIndex.setStatus(taskId.numericValue().longValue(),fi.stringValue());
            }
        }
        return isPrepared;
    }

    private IndexSearcher getSearcher(long neededCommitPoint)
        throws IOException {
        try {
            reopener.waitForGeneration(neededCommitPoint);
        } catch (InterruptedException e) {
            throw new IllegalStateException(
                "Interrupted while waiting for generation");
        }
        return sm.acquire();
    }


    private static class WarmSearchFactory extends SearcherFactory {
        public IndexSearcher newSearcher(IndexReader reader)
            throws IOException {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new IsolationSimilarity());
            return new IndexSearcher(reader);
        }
    }

    private static class IsolationSimilarity extends DefaultSimilarity {
        public IsolationSimilarity() {
        }

        public float idf(int docFreq, int numDocs) {
            return (float) 1.0;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1.0f;
        }

        public float lengthNorm(String fieldName, int numTerms) {
            return 1.0f;
        }
    }


}
