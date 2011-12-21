package org.jbpm.formbuilder.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.DoNotUseJAXBProvider;

@Path("/user")
public class RESTUserService extends RESTBaseService {

	private String[] availableRoles = new String[] { "admin", "webdesigner", "functionalanalyst" };
	
	@GET @Path("/current/roles")
    @Consumes("text/plain")
    @DoNotUseJAXBProvider
	public Response getCurrentRoles(@Context HttpServletRequest request) {
		List<String> roles = new ArrayList<String>();
		for (String role : availableRoles) {
			if (request.isUserInRole(role)) {
				roles.add(role);
			}
		}
		StringBuilder txtRoles = new StringBuilder();
		for (Iterator<String> iter = roles.iterator(); iter.hasNext(); ) {
			txtRoles.append(iter.next());
			if (iter.hasNext()) {
				txtRoles.append(",");
			}
		}
		return Response.ok(txtRoles.toString()).build();
	}
}
