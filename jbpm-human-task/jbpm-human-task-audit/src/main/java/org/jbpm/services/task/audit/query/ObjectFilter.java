package org.jbpm.services.task.audit.query;


import java.lang.reflect.Array;

/**
 * @author Hans Lund
 */
public class ObjectFilter<K, T> extends Filter<K, T> {

    private Class<T> type;

    public ObjectFilter(Occurs occurs, String field, Class<T> clazz, T... matches) {
        super(occurs, field, matches);
        type = clazz;
    }

    @Override
    public T[] getMatches() {
        return matches.toArray((T[]) Array.newInstance(type, matches.size()));
    }
}
