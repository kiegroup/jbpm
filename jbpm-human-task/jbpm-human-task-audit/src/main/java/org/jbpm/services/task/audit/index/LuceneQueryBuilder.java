package org.jbpm.services.task.audit.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
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
            return bq;
        }

        if (filters.length == 0) {
            return null;
        }

        Filter f = filters[0];
        return stdTermFilter(f, f.getField(), reader);
    }


    @SuppressWarnings("rawtypes")
    Query getQuery(Filter... filters) {
        try {
            return buildQuery(null, filters);
        } catch (IOException e) {
            throw new RuntimeException("No searcher");
        }
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
                if (r.getLower() instanceof Date) {
                    lower = DateTools.dateToString((Date) r.getLower(),
                        DateTools.Resolution.SECOND);
                    upper = DateTools.dateToString((Date) r.getUpper(),
                        DateTools.Resolution.SECOND);
                } else {
                    try {
                        List<String> lowers =
                            getTerms(field, (String) r.getLower());
                        lower = lowers.get(0);
                        List<String> uppers =
                            getTerms(field, (String) r.getUpper());
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
}




