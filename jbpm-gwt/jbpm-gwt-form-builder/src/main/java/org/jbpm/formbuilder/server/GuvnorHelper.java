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
    
}
