/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.casemgmt.impl.generator;

import org.apache.commons.lang3.StringUtils;

public final class CaseIdExpressionFunctions {

    public static CaseIdExpressionFunctions CASE_ID_FUNCTIONS = new CaseIdExpressionFunctions();

    private CaseIdExpressionFunctions() {}

    public static String PADDING(Integer count, String pad, Integer id) {
        return String.format("%" + pad + count + "d", id);
    }

    public static String PADDING(Integer count, String pad, String text) {
        return StringUtils.leftPad(text, count, pad);
    }
}