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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.queries.function.valuesource.SimpleBoolFunction;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jbpm.services.task.audit.query.ChainedComparator;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.FilterGroup;
import org.jbpm.services.task.audit.query.NumericFilter;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.jbpm.services.task.audit.query.QueryStringFilter;
import org.jbpm.services.task.audit.query.RangeFilter;
import org.jbpm.services.task.audit.query.TermFilter;
import org.jbpm.services.task.audit.query.WildCardFilter;
import org.jbpm.services.task.audit.service.StatusFilter;
import org.kie.api.task.model.Status;

/**
 * @author Hans Lund
 */
public class LuceneQueryBuilder {

    public static final String STR = "_STR";


    //If adding special filters add mapping between filter and
    //index here.
    enum FilterMapping {

        DEFAULT;
        static String getField(Filter f) {
            return f.getField();
        }

    }

    private StatusIndex statusIndex;

    public LuceneQueryBuilder(StatusIndex statusIndex) {
       this.statusIndex = statusIndex;
    }

    private Analyzer analyzer = new KeywordAnalyzer();


    <T> Sort getSort(QueryComparator<T> comparator) {
        if (comparator instanceof ChainedComparator) {
            List<SortField> sortFields = new ArrayList<SortField>();
            for (QueryComparator c : ((ChainedComparator) comparator)
                .getComparators()) {
                sortFields.add(getSortField(c));
            }
            return new Sort(
                sortFields.toArray(new SortField[sortFields.size()]));
        }
        return new Sort(getSortField(comparator));
    }

    @SuppressWarnings("rawtypes")
    private <T> SortField getSortField(QueryComparator<T> comparator) {
        String name =  comparator.getName();
        QueryComparator.Direction direction = comparator.getDirection();
        SortField.Type sortType;
        if (comparator.getType() == Status.class) {
            return new SortField(name,
                new StatusComparatorSource(), direction == QueryComparator.Direction.DESCENDING);
        } else
        if (comparator.getType() == Date.class
            || comparator.getType() == Long.class) {
            sortType = SortField.Type.LONG;
        } else if (comparator.getType() == Integer.class) {
            sortType = SortField.Type.INT;
        } else {
            sortType = SortField.Type.STRING;
        }
        return new SortField(name, sortType,
            direction == QueryComparator.Direction.DESCENDING);
    }


    /**
     * Converts a collection of <tt>Filter</tt>s into the equivalent
     * <tt>Query</tt>.
     *
     * @param reader
     * @param filters Collection of filters to convert.
     * @return Query
     */
    @SuppressWarnings("rawtypes")
    Query buildQuery(IndexSearcher reader, Filter... filters)
        throws IOException {

        if (filters.length > 1) {
            BooleanQuery bq = new BooleanQuery();
            for (Filter f : filters) {
                bq.add(buildQuery(reader, f), BooleanClause.Occur.MUST);
            }
            return new ConstantScoreQuery(bq);
        }

        if (filters.length == 0) {
            return null;
        }

        Filter f = filters[0];
        return new ConstantScoreQuery(stdTermFilter(f, f.getField(), reader));
    }


    @SuppressWarnings("rawtypes")
    private Query stdTermFilter(Filter f, String indexField,
        IndexSearcher reader) throws IOException {
        if (f instanceof FilterGroup) {
            BooleanQuery inner = new BooleanQuery();
            for (Object o : f.getMatches()) {
                inner.add(stdTermFilter((Filter) o,
                    FilterMapping.getField((Filter) o), reader),
                    (map(((Filter) o).getOccurs())));
            }
            return inner;
        }
        return analyzedQuery(indexField, f);
    }

    @SuppressWarnings("rawtypes")
    private Query analyzedQuery(String field, Filter f)
        throws IOException {
        BooleanQuery wrapper = new BooleanQuery();

        if (f instanceof WildCardFilter) {
            for (String m : ((WildCardFilter) f).getMatches()) {
                TokenStream ts = getTokenStream(field, m);
                try {
                    while (ts.incrementToken()) {
                        wrapper.add(new PrefixQuery(new Term(field,
                            ts.getAttribute(CharTermAttribute.class)
                                .toString())), BooleanClause.Occur.SHOULD);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("from string");
                }
                ts.end();
                ts.close();
            }
            return wrapper;
        } else if (f instanceof RangeFilter) {
            String lower;
            String upper;
            for (RangeFilter.Range r : ((RangeFilter) f).getMatches()) {

                if (r.getLower() instanceof Long) {
                  wrapper.add(NumericRangeQuery.newLongRange(field,(Long)r.getLower(),(Long)r.getUpper(),r.isLowIncluded(),r.isUpIncluded()),
                      BooleanClause.Occur.SHOULD);
                  return wrapper;
                } else if (r.getLower() instanceof Date) {
                    lower = DateTools.dateToString((Date) r.getLower(),
                        DateTools.Resolution.SECOND);
                    upper = DateTools.dateToString((Date) r.getUpper(),
                        DateTools.Resolution.SECOND);
                } else {
                    lower = (String) r.getLower();
                    upper = (String) r.getUpper();
                }
                try {
                    List<String> lowers =
                        getTerms(field, lower);
                    lower = lowers.get(0);
                    List<String> uppers =
                        getTerms(field, upper);
                    upper = uppers.get(0);
                    if (lowers.size() > 1 || uppers.size() > 1) {
                        throw new IllegalArgumentException(
                            "Range filter " + "query must boundaries must"
                                + " be indexed in one term");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(
                        "No io error reading from string");
                }
                wrapper.add(new TermRangeQuery(field, new BytesRef(lower),
                    new BytesRef(upper), r.isLowIncluded(), r.isUpIncluded()),
                    BooleanClause.Occur.SHOULD);
            }
            return wrapper;
        } else if (f instanceof QueryStringFilter) {
            QueryParser qp =
                new QueryParser(Version.LUCENE_47, f.getField(), analyzer);
            qp.setDefaultOperator(QueryParser.Operator.AND);
            if (f.getMatches().length > 1) {
                BooleanQuery bq = new BooleanQuery();
                for (Object s : f.getMatches()) {
                    try {
                        bq.add(qp.parse(s.toString()), map(f.getOccurs()));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(
                            "Ignored term from filter - unable to parse" + s
                                .toString(), e);
                    }
                }
                return bq;
            } else {
                try {
                    return qp.parse((String) f.getMatches()[0]);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("filter not functional",
                        e);
                }
            }
        } else if (f instanceof NumericFilter) {
            for (Long m : ((NumericFilter) f).getMatches()) {
                wrapper.add(new TermQuery(
                    new Term(f.getField() + "_str", String.valueOf(m))),
                    BooleanClause.Occur.SHOULD);
            }
        } else if (f instanceof StatusFilter) {
            wrapper.add(
                new FunctionQuery(new StatusFunction
                    (((TermFilter)f).getMatches(), new LongFieldSource("taskId"))),
                    BooleanClause.Occur.SHOULD);

        } else {
            for (String s : ((TermFilter) f).getMatches()) {
                TokenStream ts = getTokenStream(field, s);
                try {
                    while (ts.incrementToken()) {
                        wrapper.add(new TermQuery(new Term(field,
                            ts.getAttribute(CharTermAttribute.class)
                                .toString())), BooleanClause.Occur.SHOULD);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("from string");
                }
                ts.end();
                ts.close();
            }
        }
        return wrapper;
    }

    private List<String> getTerms(String field, String txt) throws IOException {
        List<String> tm = new ArrayList<String>();
        TokenStream ts = getTokenStream(field, txt);
        while (ts.incrementToken()) {
            tm.add(ts.getAttribute(CharTermAttribute.class).toString());
        }
        ts.end();
        ts.close();
        return tm;
    }

    private BooleanClause.Occur map(Filter.Occurs occurs) {
        if (Filter.Occurs.MUST == occurs) {
            return BooleanClause.Occur.MUST;
        }
        if (Filter.Occurs.NOT == occurs) {
            return BooleanClause.Occur.MUST_NOT;
        }
        return BooleanClause.Occur.SHOULD;
    }

    /**
     * Creates a <tt>TokenStream</tt> for the given value, based on the analyzer
     * settings for the specified field.
     *
     * @param field The field for which a <tt>TokenStream</tt> should be
     *              created.
     * @param value The value that the <tt>TokenStream</tt> should describe.
     * @return TokenStream
     */
    TokenStream getTokenStream(String field, String value) {
        try {
            TokenStream ts =
                analyzer.tokenStream(field, new StringReader(value));
            ts.reset();
            return ts;
        } catch (IOException e) {
            throw new RuntimeException(
                "StringReader should never throw io exception", e);
        }
    }

    private class StatusFunction extends SimpleBoolFunction {

        private Status[] stat;

        public StatusFunction(String[] stats, ValueSource source) {
            super(source);
            if (stats != null) {
                this.stat = new Status[stats.length];
            }
            for (int i = 0; i < stats.length; i++) {
                this.stat[i] = Status.valueOf(stats[i]);
            }
        }

        @Override
        protected String name() {
            return "status function";
        }

        @Override
        protected boolean func(int doc, FunctionValues vals) {
            Status indexed = statusIndex.get(vals.longVal(doc));
            for (Status s : stat) {
                if (s == indexed) return true;
            }
            return false;
        }
    }


    private class StatusFieldComparator extends FieldComparator.NumericComparator<Long> {

        private String field;
        private FieldCache.Longs currentReaderValues;
        private long[] values;
        private long bottom;
        private Long topValue;
        private FieldCache.LongParser parser;

        private StatusFieldComparator(int numHits, String field){
            super(field, null);
            this.field = field;
            values = new long[numHits];
            this.parser = FieldCache.NUMERIC_UTILS_LONG_PARSER;
        }

        private int getSort(long taskId) {
            Status s = statusIndex.get(taskId);
            if (s == null) return -1;
            return s.ordinal();
        }

        @Override
        public int compare(int slot1, int slot2) {
            final int v1 = getSort(values[slot1]);
            final int v2 =getSort(values[slot2]);
            return v1 > v2 ? 1 : v1 == v2 ? 0 : -1;
        }

        @Override
        public int compareBottom(int doc) throws IOException {
            long v3 = currentReaderValues.get(doc);
            if (docsWithField != null && v3 == 0 && !docsWithField.get(doc)) {
                v3 = missingValue;
            }
            final int v1 = getSort(bottom);
            final int v2 =getSort(v3);
            return v1 > v2 ? 1 : v1 == v2 ? 0 : -1;
        }

        @Override
        public void setBottom(int bottom) {
            this.bottom = values[bottom];
        }

        @Override
        public void setTopValue(Long value) {
            topValue = value;
        }


        @Override
        public int compareTop(int doc) {
            long docValue = currentReaderValues.get(doc);
            if (docsWithField != null && docValue == 0 && !docsWithField.get(doc)) {
                docValue = missingValue;
            }
            final int v1 = getSort(topValue);
            final int v2 =getSort(docValue);
            return v1 > v2 ? 1 : v1 == v2 ? 0 : -1;
        }

        @Override
        public void copy(int slot, int doc) throws IOException {
            long v2 = currentReaderValues.get(doc);
            if (docsWithField != null && v2 == 0 && !docsWithField.get(doc)) {
                v2 = missingValue;
            }
            values[slot] = v2;
        }

        @Override
        public FieldComparator setNextReader(AtomicReaderContext context)
            throws IOException {
            currentReaderValues = FieldCache.DEFAULT.getLongs(context.reader(), field, parser, missingValue != null);
            return super.setNextReader(context);
        }

        @Override
        public Long value(int slot) {
            return Long.valueOf(values[slot]);
        }
    }


    private class StatusComparatorSource extends FieldComparatorSource{

        @Override
        public FieldComparator<?> newComparator(String fieldname, int numHits,
            int sortPos, boolean reversed) throws IOException {
            return new StatusFieldComparator(numHits,fieldname);
        }
    }
}




