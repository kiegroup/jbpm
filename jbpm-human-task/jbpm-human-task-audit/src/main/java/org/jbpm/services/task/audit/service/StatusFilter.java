package org.jbpm.services.task.audit.service;

import java.util.List;

import org.jbpm.services.task.audit.query.TermFilter;

/**
 * @author Hans Lund
 */
public class StatusFilter<T> extends TermFilter<T> {

    public StatusFilter(String... statues) {
        super(Occurs.MUST, "status", statues);
    }

    public StatusFilter(List<String> statues) {
        this(statues.toArray(new String[statues.size()]));
    }
}
