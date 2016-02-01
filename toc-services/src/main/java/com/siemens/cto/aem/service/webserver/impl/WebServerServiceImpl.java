package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.webserver.*;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.webserver.CreateWebServerRequest;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.siemens.cto.aem.service.webserver.impl.ConfigurationTemplate.*;

public class WebServerServiceImpl implements WebServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceImpl.class);

    private final WebServerPersistenceService webServerPersistenceService;

    private final FileManager fileManager;
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    private final String HTTPD_CONF = "httpd.conf";

    public WebServerServiceImpl(final WebServerPersistenceService webServerPersistenceService,
                                final FileManager theFileManager, StateService<WebServer, WebServerReachableState> webServerStateService) {
        this.webServerPersistenceService = webServerPersistenceService;
        fileManager = theFileManager;
        this.webServerStateService = webServerStateService;
    }

    @Override
    @Transactional
    public WebServer createWebServer(final CreateWebServerRequest createWebServerRequest,
                                     final User aCreatingUser) {
        createWebServerRequest.validate();

        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id: createWebServerRequest.getGroups()) {
            groups.add(new Group(id, null));
        }
        final WebServer webServer = new WebServer(null,
                                                  groups,
                                                  createWebServerRequest.getName(),
                                                  createWebServerRequest.getHost(),
                                                  createWebServerRequest.getPort(),
                                                  createWebServerRequest.getHttpsPort(),
                                                  createWebServerRequest.getStatusPath(),
                                                  createWebServerRequest.getHttpConfigFile(),
                                                  createWebServerRequest.getSvrRoot(),
                                                  createWebServerRequest.getDocRoot());

        return webServerPersistenceService.createWebServer(webServer, aCreatingUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final Identifier<WebServer> aWebServerId) {
        return webServerPersistenceService.getWebServer(aWebServerId);
    }

    @Override
    @Transactional(readOnly = true)
    public WebServer getWebServer(final String aWebServerName) {
        return webServerPersistenceService.findWebServerByName(aWebServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> getWebServers() {
        return webServerPersistenceService.getWebServers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final String aWebServerNameFragment) {
        return webServerPersistenceService.findWebServers(aWebServerNameFragment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebServer> findWebServers(final Identifier<Group> aGroupId) {
        return webServerPersistenceService.findWebServersBelongingTo(aGroupId);
    }

    @Override
    @Transactional
    public WebServer updateWebServer(final UpdateWebServerRequest anUpdateWebServerCommand,
                                     final User anUpdatingUser) {
        anUpdateWebServerCommand.validate();

        final List<Group> groups = new LinkedList<>();
        for (Identifier<Group> id: anUpdateWebServerCommand.getNewGroupIds()) {
            groups.add(new Group(id, null));
        }
        final WebServer webServer = new WebServer(anUpdateWebServerCommand.getId(),
                                                  groups,
                                                  anUpdateWebServerCommand.getNewName(),
                                                  anUpdateWebServerCommand.getNewHost(),
                                                  anUpdateWebServerCommand.getNewPort(),
                                                  anUpdateWebServerCommand.getNewHttpsPort(),
                                                  anUpdateWebServerCommand.getNewStatusPath(),
                                                  anUpdateWebServerCommand.getNewHttpConfigFile(),
                                                  anUpdateWebServerCommand.getNewSvrRoot(),
                                                  anUpdateWebServerCommand.getNewDocRoot());

        return webServerPersistenceService.updateWebServer(webServer, anUpdatingUser.getId());
    }

    @Override
    @Transactional
    public void removeWebServer(final Identifier<WebServer> aWebServerId) {

        webServerPersistenceService.removeWebServer(aWebServerId);
    }

    @Override
    public boolean isStarted(WebServer webServer) {
        CurrentState<WebServer, WebServerReachableState> currentWSState = webServerStateService.getCurrentState(webServer.getId());
        return currentWSState.getState().equals(WebServerReachableState.WS_REACHABLE);
    }

    @Override
    @Transactional
    public void removeWebServersBelongingTo(final Identifier<Group> aGroupId) {
        webServerPersistenceService.removeWebServersBelongingTo(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateHttpdConfig(final String aWebServerName, final Boolean withSsl) {
        final WebServer server = webServerPersistenceService.findWebServerByName(aWebServerName);
        final List<Application> apps = webServerPersistenceService.findApplications(aWebServerName);
        final List<Jvm> jvms = webServerPersistenceService.findJvms(aWebServerName);

        try {
            if (withSsl != null && withSsl) {
                String httpdConfText = getResourceTemplate(aWebServerName, "httpd.conf", false);
                return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(aWebServerName, httpdConfText, server, jvms, apps);
            }
            return ApacheWebServerConfigFileGenerator
                    .getHttpdConf(aWebServerName, fileManager.getAbsoluteLocation(HTTPD_CONF_TEMPLATE), server, jvms, apps);
        } catch (IOException e) {
            LOGGER.error("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        } catch (NonRetrievableResourceTemplateContentException nrtce) {
            // TODO WHAAAAA ???? catchtrycatch - try .... ?
            LOGGER.info("Failed to retrieve resource template from the database", nrtce);
            try {
                return ApacheWebServerConfigFileGenerator
                        .getHttpdConf(aWebServerName, fileManager.getAbsoluteLocation(HTTPD_SSL_CONF_TEMPLATE), server, jvms, apps);
            } catch (IOException e) {
                LOGGER.error("Template not found", e);
                throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String generateWorkerProperties(final String aWebServerName) {
        final List<Jvm> jvms = webServerPersistenceService.findJvms(aWebServerName);
        final List<Application> apps = webServerPersistenceService.findApplications(aWebServerName);
        try {
            return ApacheWebServerConfigFileGenerator
                    .getWorkersProperties(aWebServerName, fileManager.getAbsoluteLocation(WORKERS_PROPS_TEMPLATE), jvms, apps);
        } catch (IOException e) {
            LOGGER.error("Template not found", e);
            throw new InternalErrorException(AemFaultType.TEMPLATE_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    public List<String> getResourceTemplateNames(String webServerName) {
        return webServerPersistenceService.getResourceTemplateNames(webServerName);
    }

    @Override
    @Transactional(readOnly = true)
    public String getResourceTemplate(final String webServerName,
                                      final String resourceTemplateName,
                                      final boolean tokensReplaced) {
        final String template = webServerPersistenceService.getResourceTemplate(webServerName, resourceTemplateName);
        if (tokensReplaced) {
            if (resourceTemplateName.equalsIgnoreCase(HTTPD_CONF)) {
                return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServerName,
                        template, webServerPersistenceService.findWebServerByName(webServerName),
                        webServerPersistenceService.findJvms(webServerName),
                        webServerPersistenceService.findApplications(webServerName));
            } else {
                throw new UnsupportedOperationException("Data binding for \"" + resourceTemplateName +
                        "\" template is currently not supported");
            }
        }
        return template;
    }

    @Override
    public void populateWebServerConfig(List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user,
                                        boolean overwriteExisting) {
        webServerPersistenceService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
    }

    @Override
    @Transactional
    public JpaWebServerConfigTemplate uploadWebServerConfig(UploadWebServerTemplateRequest uploadWebServerTemplateRequest, User user) {
        uploadWebServerTemplateRequest.validate();
        return webServerPersistenceService.uploadWebserverConfigTemplate(uploadWebServerTemplateRequest);
    }

    @Override
    @Transactional
    public String updateResourceTemplate(final String wsName, final String resourceTemplateName, final String template) {
        webServerPersistenceService.updateResourceTemplate(wsName, resourceTemplateName, template);
        return webServerPersistenceService.getResourceTemplate(wsName, resourceTemplateName);
    }

    @Override
    @Transactional(readOnly = true)
    public String previewResourceTemplate(final String webServerName, final String groupName, final String template) {
        // TODO: Web server name shouldn't be unique therefore we will have to use the groupName parameter in the future.
        return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServerName, template,
                webServerPersistenceService.findWebServerByName(webServerName),
                webServerPersistenceService.findJvms(webServerName),
                webServerPersistenceService.findApplications(webServerName));
    }

}
