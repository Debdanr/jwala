package com.siemens.cto.aem.ws.rest.v1.service.admin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public interface AdminServiceRest {

    @GET
    @Path("/properties/reload")
    Response reload();

    @GET
    @Path("/properties/view")
    Response view();
}
