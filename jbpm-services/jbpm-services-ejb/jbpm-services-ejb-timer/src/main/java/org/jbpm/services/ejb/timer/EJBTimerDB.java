/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.services.ejb.timer;

enum EJBTimerDB {
    ORACLE("select info from jboss_ejb_timer where utl_raw.cast_to_varchar2(utl_encode.base64_decode(utl_raw.cast_to_raw(dbms_lob.substr(info,2000,1)))) like '%?%'"),
    POSTGRESQL("select info from jboss_ejb_timerwhere decode(info, 'base64') like '%?%'");
    
    private final String query;
    
    private EJBTimerDB (String query) {
        this.query = query;
    }
    
    public String getQuery() {
        return query;
    }
}
