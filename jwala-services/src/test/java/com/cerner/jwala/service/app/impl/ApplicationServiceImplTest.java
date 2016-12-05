package com.cerner.jwala.service.app.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.resource.Entity;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.app.CreateApplicationRequest;
import com.cerner.jwala.common.request.app.UpdateApplicationRequest;
import com.cerner.jwala.common.request.app.UploadAppTemplateRequest;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.control.configuration.AemSshConfig;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionControlService;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.ApplicationServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.resource.ResourceService;
import com.cerner.jwala.service.resource.impl.ResourceGeneratorType;
import org.apache.tika.mime.MediaType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    static final String META_DATA_TEST_VALUES = "{\"deployPath\":\"./test/deploy-path/conf/CatalinaSSL/localhost\",\"contentType\":\"text/xml\",\"entity\":{\"type\":\"APPLICATION\",\"target\":\"soarcom-hct\",\"group\":\"soarcom-616\",\"parentName\":null,\"deployToJvms\":true},\"templateName\":\"hctXmlTemplate.tpl\",\"deployFileName\":\"hct.xml\"}";

    @Mock
    private ApplicationPersistenceService applicationPersistenceService;

    @Mock
    private JvmPersistenceService jvmPersistenceService;

    @Mock
    private RemoteCommandExecutor<ApplicationControlOperation> remoteCommandExecutor;

    @Mock
    private AemSshConfig aemSshConfig;

    @Mock
    private GroupService groupService;

    private ApplicationServiceImpl applicationService;

    @Mock
    private Application mockApplication;
    @Mock
    private Application mockApplication2;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    private ResourceDao mockResourceDao;

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private RemoteCommandExecutorImpl remoteCommandExecutorImpl;

    private BinaryDistributionService binaryDistributionService;

    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;

    private ArrayList<Application> applications2 = new ArrayList<>(1);

    private User testUser = new User("testUser");

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;

    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Before
    public void setUp() {
        groupId = new Identifier<Group>(1L);
        groupId2 = new Identifier<Group>(2L);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(groupId2, "the-ws-group-name-2");

        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1L));
        when(mockApplication.getWarPath()).thenReturn("the-ws-group-name/jwala-1.0.war");
        when(mockApplication.getName()).thenReturn("jwala 1.0");
        when(mockApplication.getGroup()).thenReturn(group);
        when(mockApplication.getWebAppContext()).thenReturn("/jwala");
        when(mockApplication.isSecure()).thenReturn(true);

        when(mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/jwala-1.1.war");
        when(mockApplication2.getName()).thenReturn("jwala 1.1");
        when(mockApplication2.getGroup()).thenReturn(group2);
        when(mockApplication2.getWebAppContext()).thenReturn("/jwala");
        when(mockApplication2.isSecure()).thenReturn(false);

        applications2.add(mockApplication);
        applications2.add(mockApplication2);

        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short) 0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());

        SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        aemSshConfig = mock(AemSshConfig.class);
        when(mockSshConfig.getUserName()).thenReturn("mockUser");
        when(aemSshConfig.getSshConfiguration()).thenReturn(mockSshConfig);

        groupService = mock(GroupService.class);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(group);

        applicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, groupService,
                mockResourceService, remoteCommandExecutorImpl, binaryDistributionService, mockHistoryFacadeService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleGet() {
        when(applicationPersistenceService.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        final Application application = applicationService.getApplication(new Identifier<Application>(1L));
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());
    }

    @Test
    public void testGetApplicationByName() {
        when(applicationPersistenceService.getApplication(anyString())).thenReturn(mockApplication);
        final Application application = applicationService.getApplication("jwala 1.0");
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());
    }

    @Test
    public void testAllGet() {
        when(applicationPersistenceService.getApplications()).thenReturn(applications2);
        final List<Application> apps = applicationService.getApplications();
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(0);
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("jwala 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/jwala-1.0.war", application.getWarPath());

        application = apps.get(1);
        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("jwala 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/jwala-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByGroupId() {
        when(applicationPersistenceService.findApplicationsBelongingTo(any(Identifier.class))).thenReturn(applications2);
        final List<Application> apps = applicationService.findApplications(groupId);
        assertEquals(applications2.size(), apps.size());

        Application application = apps.get(1);

        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("jwala 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/jwala-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BadRequestException.class)
    public void testCreateBadRequest() {
        when(applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "", "", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        when(applicationPersistenceService.createApplication(any(CreateApplicationRequest.class))).thenReturn(mockApplication2);

        CreateApplicationRequest cac = new CreateApplicationRequest(Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() {
        when(applicationPersistenceService.updateApplication(any(UpdateApplicationRequest.class))).thenReturn(mockApplication2);

        UpdateApplicationRequest cac = new UpdateApplicationRequest(mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan", true, true, false);
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testRemove() {
        applicationService.removeApplication(mockApplication.getId(), testUser);

        verify(applicationPersistenceService, Mockito.times(1)).removeApplication(Mockito.any(Identifier.class));
    }

    @Test
    public void testGetResourceTemplateNames() {
        final String[] nameArray = {"hct.xml"};
        when(applicationPersistenceService.getResourceTemplateNames(eq("hct"), anyString())).thenReturn(Arrays.asList(nameArray));
        final List names = applicationService.getResourceTemplateNames("hct", "any");
        assertEquals("hct.xml", names.get(0));
    }

    @Test
    public void testGetResourceTemplate() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        assertEquals(theTemplate, applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), false));
    }

    @Test
    public void testGetResourceTemplateWithTokensReplaced() {
        final String theTemplate = "<context>${webApp.warPath}</context>";
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        final Application app = mock(Application.class);
        when(app.getWarPath()).thenReturn("theWarPath");
        when(applicationPersistenceService.findApplication(eq("hct"), anyString(), anyString())).thenReturn(app);
        when(jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(null);
        applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), true);
        verify(mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), any(ResourceGeneratorType.class));
        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm1"), eq("group1"))).thenReturn(theTemplate);
        try {
            applicationService.getResourceTemplate("hct", "group1", "jvm1", "hct.xml", new ResourceGroup(), true);
        } catch (ApplicationException ae) {
            assertTrue(ae.getMessage().contains("replacement failed"));
        }
    }

    @Test
    public void testUpdateResourceTemplate() {
        applicationService.updateResourceTemplate("hct", "hct.xml", "content", "jvm1", "group1");
        verify(applicationPersistenceService).updateResourceTemplate(eq("hct"), eq("hct.xml"), eq("content"), eq("jvm1"), eq("group1"));
    }

    @Test
    public void testDeployConf() throws CommandFailureException, IOException {
        final Jvm jvm = mock(Jvm.class);
        when(jvm.getHostName()).thenReturn("localhost");
        when(jvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(jvmPersistenceService.findJvmByExactName(eq("jvm-1"))).thenReturn(jvm);
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), any(ApplicationControlOperation.class), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CREATE_DIRECTORY), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        when(applicationPersistenceService.getResourceTemplate(eq("hct"), eq("hct.xml"), eq("jvm-1"), eq("hct-group"))).thenReturn("Test template");
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(applicationPersistenceService.getMetaData(anyString(), anyString(), anyString(), anyString())).thenReturn(META_DATA_TEST_VALUES);

        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);

        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        when(mockMetaData.getDeployFileName()).thenReturn("hct.xml");
        when(mockMetaData.getDeployPath()).thenReturn("./test/deploy-path/conf/CatalinaSSL/localhost");
        when(mockMetaData.getContentType()).thenReturn(MediaType.APPLICATION_XML);
        when(mockResourceService.getTokenizedMetaData(anyString(), any(Object.class), anyString())).thenReturn(mockMetaData);
        when(mockResourceService.generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(), any(ResourceGeneratorType.class))).thenReturn("{\"deployPath\":\"./test/deploy-path/conf/CatalinaSSL/localhost\",\"contentType\":\"text/xml\",\"entity\":{\"type\":\"APPLICATION\",\"target\":\"soarcom-hct\",\"group\":\"soarcom-616\",\"parentName\":null,\"deployToJvms\":true},\"templateName\":\"hctXmlTemplate.tpl\",\"deployFileName\":\"hct.xml\"}");
        when(mockResourceService.generateAndDeployFile(any(ResourceIdentifier.class), anyString(), anyString(), anyString())).thenReturn(execData);

        CommandOutput retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(mockApplication.isSecure()).thenReturn(false);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        when(mockApplication.isSecure()).thenReturn(true);
        retExecData = applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        assertTrue(retExecData.getReturnCode().wasSuccessful());

        // test errors
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(execData.getStandardError()).thenReturn("REMOTE COMMAND FAILURE");
        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertEquals("REMOTE COMMAND FAILURE", ee.getMessage());
        }

        when(remoteCommandExecutorImpl.executeRemoteCommand(
                anyString(), anyString(), eq(ApplicationControlOperation.SECURE_COPY), any(WindowsApplicationPlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("fail me"), new Throwable("should fail")));
        when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), eq(ApplicationControlOperation.CHECK_FILE_EXISTS), any(WindowsApplicationPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", ""));
        try {
            applicationService.deployConf("hct", "hct-group", "jvm-1", "hct.xml", mock(ResourceGroup.class), testUser);
        } catch (DeployApplicationConfException ee) {
            assertTrue(ee.getCause() instanceof CommandFailureException);
        }

    }

    @Test(expected = InternalErrorException.class)
    public void testDeployConfJvmNotStopped() {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        when(jvmPersistenceService.findJvm(anyString(), anyString())).thenReturn(mockJvm);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(mockApplication);
        when(applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("IGNORED CONTENT");
        applicationService.deployConf("testApp", "testGroup", "testJvm", "HttpSslConfTemplate.tpl", mock(ResourceGroup.class), testUser);
    }

    @Test
    public void testPreviewResourceTemplate() {
        final Jvm jvm = mock(Jvm.class);
        when(applicationPersistenceService.findApplication(eq("hct"), eq("hct-group"), eq("jvm-1"))).thenReturn(mockApplication);
        when(jvmPersistenceService.findJvm(eq("jvm-1"), eq("hct-group"))).thenReturn(jvm);
        final String preview = applicationService.previewResourceTemplate("myFile", "hct", "hct-group", "jvm-1", "Template contents", new ResourceGroup());
        verify(mockResourceService).generateResourceFile(anyString(), anyString(), any(ResourceGroup.class), any(Application.class), any(ResourceGeneratorType.class));
    }

    @Test
    public void testUploadTemplate() {
        final UploadAppTemplateRequest cmd = mock(UploadAppTemplateRequest.class);
        when(cmd.getConfFileName()).thenReturn("roleMapping.properties");
        when(cmd.getJvmName()).thenReturn("testJvmName");
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(111L));
        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(mockJvm);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        when(jvmPersistenceService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        applicationService.uploadAppTemplate(cmd);
        verify(cmd).validate();
        verify(applicationPersistenceService).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        List<Jvm> jvmList = new ArrayList<>();
        jvmList.add(mockJvm);
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(cmd.getConfFileName()).thenReturn("hct.xml");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(2)).validate();
        verify(applicationPersistenceService, times(2)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

        when(mockJvm.getJvmName()).thenReturn("notTestJvmName");
        applicationService.uploadAppTemplate(cmd);
        verify(cmd, times(3)).validate();
        verify(applicationPersistenceService, times(3)).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));

    }

    @Test
    public void testFindApplicationsByJvmId() {
        final Identifier<Jvm> id = new Identifier<Jvm>(1l);
        applicationService.findApplicationsByJvmId(id);
        verify(applicationPersistenceService).findApplicationsBelongingToJvm(eq(id));
    }


    @Test
    public void testCopyApplicationToGroupHosts() throws IOException {
        final HashSet<Jvm> jvmSet = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        Jvm mockJvm2 = mock(Jvm.class);
        jvmSet.add(mockJvm);
        jvmSet.add(mockJvm2);
        Group mockGroup = mock(Group.class);
        GroupService mockGroupService = mock(GroupService.class);
        final Identifier<Group> mockGroupId = new Identifier<>(999L);
        when(mockGroup.getId()).thenReturn(mockGroupId);
        when(mockGroup.getJvms()).thenReturn(jvmSet);
        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplication.getWarName()).thenReturn("test.war");
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        when(mockJvm.getHostName()).thenReturn("localhost");
        when(mockJvm2.getHostName()).thenReturn("localhost");

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, mockGroupService,
                mockResourceService, remoteCommandExecutorImpl, binaryDistributionService, mockHistoryFacadeService);

        try {
            CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successCommandOutput);
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        }
        new File("./src/test/resources/webapps/test.war").delete();

        try {
            CommandOutput failedCommandOutput = new CommandOutput(new ExecReturnCode(1), "FAILED", "");
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(failedCommandOutput);
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        } catch (InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }
        new File("./src/test/resources/webapps/test.war").delete();

        boolean exceptionThrown = false;
        try {
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("FAILED"), new Throwable("Expected to fail test")));
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive_FAIL_COPY.war");
        exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

    }

    @Test
    public void testCopyApplicationWarToHost() {
        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive.war");
        when(mockApplication.getWarName()).thenReturn("test.war");
        CommandOutput successCommandOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, null,
                mockResourceService, remoteCommandExecutorImpl, binaryDistributionService, mockHistoryFacadeService);

        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString(), anyString())).thenReturn(successCommandOutput);
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(BinaryDistributionControlService.class), any(PlatformCommandProvider.class), anyString(), anyString(),anyString())).thenReturn(successCommandOutput);
            mockApplicationService.copyApplicationWarToHost(mockApplication, "localhost");
        } catch (CommandFailureException e) {
            assertTrue("should not fail " + e.getMessage(), false);
        }
        new File("./src/test/resources/webapps/test.war").delete();

        boolean exceptionThrown = false;
        try {
            when(remoteCommandExecutorImpl.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenThrow(new CommandFailureException(new ExecCommand("FAILED"), new Throwable("Expected to fail test")));
            mockApplicationService.copyApplicationWarToHost(mockApplication, "localhost");
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        new File("./src/test/resources/webapps/test.war").delete();

        when(mockApplication.getWarPath()).thenReturn("./src/test/resources/archive/test_archive_FAIL_COPY.war");
        exceptionThrown = false;
        try {
            mockApplicationService.copyApplicationWarToGroupHosts(mockApplication);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testCopyToGroupJvms() throws CommandFailureException {
        GroupService mockGroupService = mock(GroupService.class);
        Group mockGroup = mock(Group.class);
        when(mockGroupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockGroupService.getGroup(anyString())).thenReturn(mockGroup);
        Set<Group> groupSet = new HashSet<>();
        groupSet.add(mockGroup);

        Set<Jvm> jvms = new HashSet<>();
        final Jvm testjvm = mock(Jvm.class);
        when(testjvm.getId()).thenReturn(new Identifier<Jvm>(11111L));
        when(testjvm.getJvmName()).thenReturn("testjvm");
        when(testjvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        jvms.add(testjvm);

        when(jvmPersistenceService.findJvmByExactName(anyString())).thenReturn(testjvm);
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockGroup.getName()).thenReturn("testGroupName");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("app.xml");
        when(applicationPersistenceService.getResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(applicationPersistenceService.findApplication(anyString(), anyString(), anyString())).thenReturn(new Application(new Identifier<Application>(111L), "appName", "./warPath", "/context", mockGroup, true, true, false, "app.war"));
        when(applicationPersistenceService.getResourceTemplate(anyString(), anyString(), anyString(), anyString())).thenReturn("template this!");
        when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), any(ApplicationControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Success!", ""));

        ApplicationServiceImpl mockApplicationService = new ApplicationServiceImpl(applicationPersistenceService,
                jvmPersistenceService, mockGroupService,
                mockResourceService, remoteCommandExecutorImpl, binaryDistributionService, mockHistoryFacadeService);
        mockApplicationService.copyApplicationConfigToGroupJvms(mockGroup, "testApp", mock(ResourceGroup.class), testUser);

    }

    @Test
    public void testAppDeployConf() throws IOException {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        Set<Jvm> jvms = new HashSet<>();
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        Entity mockEntity = mock(Entity.class);
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(mockApplication.getName()).thenReturn(appName);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getName()).thenReturn("test-group");
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(groupService.getHosts(anyString())).thenReturn(hosts);
        when(groupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("");
        when(mockResourceService.getMetaData(anyString())).thenReturn(mockMetaData);
        when(mockMetaData.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getDeployToJvms()).thenReturn(false);
        when(groupService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(groupService.deployGroupAppTemplate(anyString(), anyString(), any(Application.class), anyString())).thenReturn(mockCommandOutput);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(0));
        applicationService.deployConf(appName, null, testUser);
        verify(groupService, times(2)).deployGroupAppTemplate(eq("test-group"), anyString(), eq(mockApplication), anyString());
    }

    @Test (expected = InternalErrorException.class)
    public void testAppDeployConfCommandFailure() throws IOException {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        Set<Jvm> jvms = new HashSet<>();
        ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);
        Entity mockEntity = mock(Entity.class);
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(mockApplication.getName()).thenReturn(appName);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(groupService.getHosts(anyString())).thenReturn(hosts);
        when(groupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("");
        when(mockResourceService.getMetaData(anyString())).thenReturn(mockMetaData);
        when(mockMetaData.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getDeployToJvms()).thenReturn(false);
        when(groupService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        when(groupService.deployGroupAppTemplate(anyString(), anyString(), any(Application.class), anyString())).thenReturn(mockCommandOutput);
        when(mockCommandOutput.getReturnCode()).thenReturn(new ExecReturnCode(1));
        applicationService.deployConf(appName, null, testUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testAppDeployConfNoHostFailure() {
        final String appName = "test-app";
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(group);
        when(mockApplication.getGroup()).thenReturn(group);
        when(groupService.getHosts(anyString())).thenReturn(null);
        applicationService.deployConf(appName, null, testUser);
    }

    @Test (expected = ApplicationServiceException.class)
    public void testAppDeployConfJvmStatedFailure() {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        Set<Jvm> jvms = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(groupService.getHosts(anyString())).thenReturn(hosts);
        applicationService.deployConf(appName, null, testUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testAppDeployConfIncorrectHostFailure() {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(group);
        when(mockApplication.getGroup()).thenReturn(group);
        when(groupService.getHosts(anyString())).thenReturn(hosts);
        applicationService.deployConf(appName, "test", testUser);
    }

    @Test (expected = InternalErrorException.class)
    public void testAppDeployConfResourceTemplateFailure() throws IOException {
        final String appName = "test-app";
        List<String> hosts = new ArrayList<>();
        hosts.add("testServer");
        hosts.add("testServer2");
        List<String> templateNames = new ArrayList<>();
        templateNames.add("");
        Set<Jvm> jvms = new HashSet<>();
        Jvm mockJvm = mock(Jvm.class);
        jvms.add(mockJvm);
        Group mockGroup = mock(Group.class);
        when(applicationPersistenceService.getApplication(eq(appName))).thenReturn(mockApplication);
        when(groupService.getGroup(any(Identifier.class))).thenReturn(mockGroup);
        when(mockApplication.getGroup()).thenReturn(mockGroup);
        when(mockGroup.getId()).thenReturn(new Identifier<Group>(1L));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(mockJvm.getHostName()).thenReturn("testserver");
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        when(groupService.getHosts(anyString())).thenReturn(hosts);
        when(groupService.getGroupAppResourceTemplateMetaData(anyString(), anyString())).thenReturn("");
        when(mockResourceService.getMetaData(anyString())).thenThrow(IOException.class);
        when(groupService.getGroupAppsResourceTemplateNames(anyString(), anyString())).thenReturn(templateNames);
        applicationService.deployConf(appName, "testserver", testUser);
    }

}
