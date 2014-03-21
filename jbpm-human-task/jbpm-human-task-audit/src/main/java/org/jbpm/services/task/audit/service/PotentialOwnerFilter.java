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

package org.jbpm.services.task.audit.service;

import java.util.StringTokenizer;

import org.jbpm.services.task.audit.query.TermFilter;

/**
 * @author Hans Lund
 */
public class PotentialOwnerFilter<T> extends TermFilter<T> {

    public PotentialOwnerFilter(String... terms) {
        super(Occurs.MUST, "potentialOwners");
        if (terms.length == 1) {
            StringTokenizer tok = new StringTokenizer(terms[0], "|");
            while (tok.hasMoreElements()) {
                add(tok.nextToken());
            }
        } else {
            for (String string : terms) {
                add(string);
            }
        }
    }
}
