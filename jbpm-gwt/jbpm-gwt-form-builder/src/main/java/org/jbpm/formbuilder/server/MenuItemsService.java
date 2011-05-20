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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/*
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("menuItems")*/
public class MenuItemsService {

   /* @GET
    @Produces({"application/json"})
    public Response listMenuItemsClasses() {
        Gson gson = new GsonBuilder().create();
        
        List<String> classNames = new ArrayList<String>();
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("FormBuilder.menuItems.conf"));
        } catch (Exception e) {
            return Response.serverError().build();
        }

        for (Object key : props.keySet()) {
            String className = key.toString();
            classNames.add(className);
        }
        
        String json = gson.toJson(new StringListWrapper(classNames));
        return Response.ok(json).type("application/json").build();
    } */
}
