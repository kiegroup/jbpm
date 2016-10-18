/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.migration;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for making the error handling within the parsing and validation processes a little more verbose.
 */
abstract class ErrorCollector<T extends Exception> {
    private final List<T> warningList = new ArrayList<T>();
    private final List<T> errorList = new ArrayList<T>();

    public void warning(final T ex) {
        warningList.add(ex);
    }

    public void error(final T ex) {
        errorList.add(ex);
    }

    public boolean didErrorOccur() {
        // checking warnings might be too restrictive
        return !warningList.isEmpty() || !errorList.isEmpty();
    }

    public List<T> getWarningList() {
        return warningList;
    }

    public List<T> getErrorList() {
        return errorList;
    }

    public void logErrors(final Logger logger) {
        for (final T ex : warningList) {
            logger.warn("==>", ex);
        }
        for (final T ex : errorList) {
            logger.error("==>", ex);
        }
    }
}
