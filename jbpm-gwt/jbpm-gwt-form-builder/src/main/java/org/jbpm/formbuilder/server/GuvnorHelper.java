/**
 * Copyright 2011 JBoss Inc 
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
package org.jbpm.formbuilder.server;

import org.apache.commons.codec.binary.Base64;

public class GuvnorHelper {
    
    private final String baseUrl;
    private final String user;
    private final String password;
    
    public GuvnorHelper(String baseUrl, String user, String password) {
        this.baseUrl = baseUrl;
        this.user = user;
        this.password = password;
    }

    public String getAuth() {
        String basic = this.user + ":" + this.password;
        basic = "BASIC " + Base64.encodeBase64(basic.getBytes());
        return basic;
    }
    
    public String getApiUrl(String pkgName) {
        return new StringBuilder(this.baseUrl).
            append("/org.drools.guvnor.Guvnor/api/package/").
            append(pkgName).append("/").toString();
    }

    public String getApiSearchUrl(String pkgName) {
        return new StringBuilder(this.baseUrl).
            append("/org.drools.guvnor.Guvnor/api/packages/").
            append(pkgName).append("/").toString();
    }

    public String getRestBaseUrl() {
        return new StringBuilder(this.baseUrl).append("/rest/packages/").toString();
    }
    
}
