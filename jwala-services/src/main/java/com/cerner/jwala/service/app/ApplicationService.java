package com.cerner.jwala.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.common.request.app.UploadWebArchiveRequest;
import com.cerner.jwala.persistence.jpa.domain.JpaApplicationConfigTemplate;

import java.io.IOException;
import java.util.List;

public interface ApplicationService {

    Application getApplication(Identifier<Application> aApplicationId);

    Application getApplication(String name);

    Application updateApplication(UpdateApplicationRequest anAppToUpdate, User user);

    Application createApplication(CreateApplicationRequest anAppToCreate, User user);

    void removeApplication(Identifier<Application> anAppIdToRemove, User user);

    List<Application> getApplications();

    List<Application> findApplications(Identifier<Group> groupId);

    List<Application> findApplicationsByJvmId(Identifier<Jvm> jvmId);

    Application uploadWebArchive(UploadWebArchiveRequest command, User user);

    Application deleteWebArchive(Identifier<Application> appToRemoveWAR, User user);

    List<String> getResourceTemplateNames(final String appName, String jvmName);

    String getResourceTemplate(final String appName, String groupName, String jvmName, final String resourceTemplateName,
                               final ResourceGroup resourceGroup, final boolean tokensReplaced);

    String updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, final String jvmName, final String groupName);

    /**
     * Deploy a configuration file.
     *  @param appName              - the application name.
     * @param groupName
     * @param jvmName              - the jvm name where the application resides.
     * @param resourceTemplateName - the resource template in which the configuration file is based on.
     * @param resourceGroup
     * @param user                 - the user.    @return {@link CommandOutput}
     */
    CommandOutput deployConf(String appName, String groupName, String jvmName, String resourceTemplateName, ResourceGroup resourceGroup, User user);

    JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest command);

    /**
     * Gets a preview of a resource file.
     *
     * @param appName   application name
     * @param groupName group name
     * @param jvmName   JVM name
     * @param template  the template to preview.
     * @param resourceGroup
     * @return The resource file preview.
     */
    String previewResourceTemplate(String appName, String groupName, String jvmName, String template, ResourceGroup resourceGroup);

    void copyApplicationWarToGroupHosts(Application application);

    void copyApplicationWarToHost(Application application, String hostName);

    void copyApplicationConfigToGroupJvms(Group group, String appName, ResourceGroup resourceGroup, User user);

    void deployApplicationResourcesToGroupHosts(String groupName, Application app, ResourceGroup resourceGroup);

    /**
     * Upload a WAR for an application.
     * @param appId the application id
     * @param warName the war name
     * @param war the war byte data
     * @param deployPath @return {@link Application}
     */
    Application uploadWebArchive(final Identifier<Application> appId, String warName, byte[] war, String deployPath) throws IOException;
}