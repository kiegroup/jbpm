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

package org.jbpm.services.task.audit.query;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Hans Lund
 */
public class DateRangeFilter<T> extends RangeFilter<T, Long> {


    private static ArrayBlockingQueue<Calendar> calendars =
        new ArrayBlockingQueue<Calendar>(5,false);
    static {
        while (calendars.offer(Calendar.getInstance()));
    }

    public DateRangeFilter(Occurs occurs, String field) {
        super(occurs, field);
    }

    public boolean addRange(Date lower, Date upper) {
        return super.addRange(getStartOfDay(lower), getEndOfDay(upper));
    }

    private long getStartOfDay(Date time) {
        Calendar cal = calendars.poll();
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        cal.setTime(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long t = cal.getTimeInMillis();
        calendars.offer(cal);
        return t;
    }

    private long getEndOfDay(Date time) {
        Calendar cal = calendars.poll();
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        cal.setTime(time);
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long t = cal.getTimeInMillis();
        calendars.offer(cal);
        return t;
    }
}
