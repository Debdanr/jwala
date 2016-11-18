package com.cerner.jwala.ws.rest.v1.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.ws.rest.v1.provider.AuthenticatedUser;
import com.cerner.jwala.ws.rest.v1.service.app.impl.JsonCreateApplication;
import com.cerner.jwala.ws.rest.v1.service.app.impl.JsonUpdateApplication;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.springframework.beans.factory.InitializingBean;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/applications")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationServiceRest extends InitializingBean {

    @GET
    Response getApplications(@QueryParam("group.id") final Identifier<Group> aGroupId);

    @GET
    @Path("/{applicationId}")
    Response getApplication(@PathParam("applicationId") final Identifier<Application> anAppId);

    @GET
    @Path("/application")
    Response getApplicationByName(@MatrixParam("name") String name);

    @GET
    @Path("/jvm/{jvmId}")
    Response findApplicationsByJvmId(@PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createApplication(final JsonCreateApplication anAppToCreate,
                               @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateApplication(final JsonUpdateApplication appsToUpdate,
                               @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{applicationId}")
    Response removeApplication(@PathParam("applicationId") final Identifier<Application> anAppToRemove,
                               @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{applicationId}/war")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadWebArchive(@PathParam("applicationId") final Identifier<Application> appId,
                              @Context MessageContext messageContext);

    @DELETE
    @Path("/{applicationId}/war")
    Response deleteWebArchive(@PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Path("/{applicationId}/war/deploy")
    Response deployWebArchive(@PathParam("applicationId") final Identifier<Application> anAppToGet,
                              @BeanParam final AuthenticatedUser aUser);
    
    @PUT
    @Path("/{applicationId}/war/deploy/{hostName}")
    Response deployWebArchive(@PathParam("applicationId") final Identifier<Application> anAppToGet,
    						  @PathParam("hostName") String hostName);

    @GET
    @Path("/{jvmName}/{appName}/resources/name")
    Response getResourceNames(@PathParam("appName") String appName, @PathParam("jvmName") String jvmName);

    /**
     * Get resource template content.
     *
     * @param appName              web application name.
     * @param resourceTemplateName the resource template name.
     * @param tokensReplaced       flag that indicates whether to fetch the template with its tokens replaced by their mapped values from the db.
     * @return the template contents
     */
    @GET
    @Path("/{appName}/resources/template/{resourceTemplateName}")
    Response getResourceTemplate(@PathParam("appName") String appName,
                                 @MatrixParam("groupName") String groupName,
                                 @MatrixParam("jvmName") String jvmName,
                                 @PathParam("resourceTemplateName") String resourceTemplateName,
                                 @QueryParam("tokensReplaced") boolean tokensReplaced);

    @PUT
    @Path("/{appName}/resources/template/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response updateResourceTemplate(@PathParam("appName") final String appName,
                                    @PathParam("resourceTemplateName") final String resourceTemplateName,
                                    @MatrixParam("jvmName") final String jvmName,
                                    @MatrixParam("groupName") final String groupName,
                                    final String content);

    @PUT
    @Path("/{appName}/conf/{resourceTemplateName}")
    Response deployConf(@PathParam("appName") String appName,
                        @MatrixParam("groupName") String groupName,
                        @MatrixParam("jvmName") String jvmName,
                        @PathParam("resourceTemplateName") String resourceTemplateName,
                        @BeanParam AuthenticatedUser aUser);

    @PUT
    @Path("/{appName}/resources/preview/{resourceTemplateName}")
    @Consumes(MediaType.TEXT_PLAIN)
    Response previewResourceTemplate(@PathParam("appName") String appName,
                                     @MatrixParam("groupName") String groupName,
                                     @MatrixParam("jvmName") String jvmName,
                                     @PathParam("resourceTemplateName") String resourceTemplateName,
                                     String template);

    @PUT
    @Path("/{appName}/conf")
    Response deployConf(@PathParam("appName") String appName, @BeanParam AuthenticatedUser aUser, @QueryParam("hostName") String hostName);

}
