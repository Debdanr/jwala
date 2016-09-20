package com.cerner.jwala.service.webserver;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.request.webserver.CreateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UpdateWebServerRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;

import java.util.List;

public interface WebServerService {

    WebServer createWebServer(final CreateWebServerRequest aCreateWebServerCommand, final User aCreatingUser);

    WebServer getWebServer(final Identifier<WebServer> aWebServerId);

    WebServer getWebServer(final String aWebServerName);

    List<WebServer> getWebServers();

    List<WebServer> findWebServers(final Identifier<Group> aGroupId);

    WebServer updateWebServer(final UpdateWebServerRequest anUpdateWebServerCommand, final User anUpdatingUser);

    void removeWebServer(final Identifier<WebServer> aWebServerId);

    String generateInvokeWSBat(WebServer webServer);

    String generateHttpdConfig(final String aWebServerName, ResourceGroup resourceGroup);

    List<String> getResourceTemplateNames(final String webServerName);

    String getResourceTemplate(final String webServerName, final String resourceTemplateName, final boolean tokensReplaced, ResourceGroup resourceGroup);

    void uploadWebServerConfig(UploadWebServerTemplateRequest uploadWebServerTemplateCommand, User user);

    String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template);

    String previewResourceTemplate(String webServerName, String groupName, String template);

    boolean isStarted(WebServer webServer);

    void updateErrorStatus(Identifier<WebServer> id, String errorStatus);

    void updateState(Identifier<WebServer> id, WebServerReachableState state, String errorStatus);

    Long getWebServerStartedCount(String groupName);

    Long getWebServerCount(String groupName);

    List<WebServer> getWebServersPropagationNew();

    Long getWebServerStoppedCount(String groupName);

    String getResourceTemplateMetaData(String aWebServerName, String resourceTemplateName);
}