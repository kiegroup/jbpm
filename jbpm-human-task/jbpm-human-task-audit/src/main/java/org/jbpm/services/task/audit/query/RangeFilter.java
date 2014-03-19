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

import java.lang.reflect.Array;

/**
 * @author Hans Lund
 */
public class RangeFilter<K, T> extends Filter<K, RangeFilter.Range> {

    private Class rangeType;

    public RangeFilter(Occurs occurs, String field) {
        super(occurs, field);
        rangeType = this.new Range().getClass();
    }

    @Override
    public Range[] getMatches() {
        return matches
            .toArray((Range[]) Array.newInstance(rangeType, matches.size()));
    }

    public boolean matches(Object value) {
        for (Range r : getMatches()) {
            //lower bound check
            int l = ((Comparable) value).compareTo(r.lower);
            if (l < 0) {
                continue;
            } else if (l == 0 && !r.includeLow) {
                continue;
            }
            //upper bound check
            int h = ((Comparable) value).compareTo(r.upper);
            if (h > 0) {
                continue;
            } else if (h == 0 && !r.includeUp) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a Range including both ends.
     * @return true if added, false if duplicate.
     */
    public boolean addRange(T lower, T upper) {
        return this.add(new Range(lower, upper, true, true));
    }

    public boolean addRange(T lower, T upper, boolean includeLow,
        boolean includeUp) {
        return this.add(new Range(lower, upper, includeLow, includeUp));
    }

    public class Range {

        boolean includeLow;
        boolean includeUp;
        private T lower;
        private T upper;

        private Range() {
        }

        private Range(T lower, T upper, boolean includeLow, boolean includeUp) {
            this.lower = lower;
            this.upper = upper;
            this.includeLow = includeLow;
            this.includeUp = includeUp;
        }

        /**
         * Gets the lower limit for the range.
         *
         * @return lower limit.
         */
        public T getLower() {
            return lower;
        }

        /**
         * Gets upper limit for the range.
         *
         * @return upper limit.
         */
        public T getUpper() {
            return upper;
        }

        /**
         * Limits can be inclusive or exclusive, this tells the state of the
         * lower limit.
         *
         * @return <code>true</code> if the lower limit is part of the filter.
         */
        public boolean isLowIncluded() {
            return includeLow;
        }

        /**
         * Limits can be inclusive or exclusive, this tells the state of the
         * upper limit.
         *
         * @return <code>true</code> if the upper limit is part of the filter.
         */
        public boolean isUpIncluded() {
            return includeUp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Range range = (Range) o;

            if (includeLow != range.includeLow) {
                return false;
            }
            if (includeUp != range.includeUp) {
                return false;
            }
            if (lower != null ? !lower.equals(range.lower)
                : range.lower != null) {
                return false;
            }
            if (upper != null ? !upper.equals(range.upper)
                : range.upper != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = lower != null ? lower.hashCode() : 0;
            result = 31 * result + (upper != null ? upper.hashCode() : 0);
            result = 31 * result + (includeLow ? 1 : 0);
            result = 31 * result + (includeUp ? 1 : 0);
            return result;
        }
    }
}
