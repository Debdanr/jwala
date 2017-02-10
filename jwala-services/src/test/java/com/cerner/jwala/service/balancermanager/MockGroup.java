package com.cerner.jwala.service.balancermanager;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.media.Media;
import com.cerner.jwala.common.domain.model.path.FileSystemPath;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;

import java.util.*;

import static com.cerner.jwala.common.domain.model.id.Identifier.id;

public class MockGroup {

    private Identifier<Group> groupId = new Identifier<>((long) 1);
    private String groupName = "mygroupName";
    private Group parentGroup;

    public Group getGroup() {
        getJvms();
        getApplications();
        getWebServers();
        parentGroup = new Group(groupId,
                groupName,
                jvms,
                webServers,
                null,
                applications);
        this.groups.add(parentGroup);
        return parentGroup;
    }

    public List<Jvm> getJvms() {
        List<Jvm> jvms = new LinkedList<>();
        Jvm jvm = new Jvm(id(0L, Jvm.class),
                "jvmname",
                "somehost0057",
                groups,
                parentGroup,
                9100,
                9101,
                9102,
                -1,
                9103,
                new Path("statusPath"),
                "systemProperties",
                JvmState.JVM_START,
                "errorStatus",
                Calendar.getInstance(),
                "username",
                "encryptedpassword",
                new Media(1, "JDK 1.7-test", "/local/archive/path.zip", "JDK", "/remote/host/path", "unzipped-root-deploy-dir"),
                new Media(2, "Apache Tomcat 7.0.55-test", "/local/archive/path.zip", "Tomcat", "/remote/host/path", "unzipped-root-deploy-dir"),
                "d:/java/home", null);
        this.jvms.add(jvm);
        jvms.add(jvm);
        return jvms;
    }

    public Jvm getJvm(final String jvmName) {
        Jvm jvm = new Jvm(id(0L, Jvm.class),
                jvmName,
                "somehost0057",
                groups,
                parentGroup,
                9100,
                9101,
                9102,
                -1,
                9103,
                new Path("statusPath"),
                "systemProperties",
                JvmState.JVM_START,
                "errorStatus",
                Calendar.getInstance(),
                "username",
                "encryptedpassword",
                new Media(1, "JDK 1.7-test", "/local/archive/path", "JDK", "/remote/host/path", "unzipped-root-deploy-dir"),
                new Media(1, "Apache Tomcat 7.0.55-test", "/local/archive/path", "Tomcat", "/remote/host/path", "unzipped-root-deploy-dir"),
                "d:/java/home", null);
        return jvm;
    }

    public void getWebServers() {
        Group myGroup = new Group(groupId, groupName);
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "localhost",
                "myWebServerName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        webServers.add(webServer);
    }

    public WebServer getWebServer(final String webServerName) {
        Group myGroup = new Group(groupId, groupName);
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "localhost",
                "myWebSererName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        return webServer;
    }

    public List<Application> getApplications() {
        Group myGroup = new Group(groupId, groupName, jvms);
        List<Application> applications = new LinkedList<>();
        Application application = new Application(id(0L, Application.class),
                "HEALTH-CHECK-4.0",
                "myaWarPath",
                "/hct",
                myGroup,
                true,
                false,
                false,
                "myWarName");
        List<Jvm> jvmsList = new LinkedList<>();
        for (Jvm jvm : jvms) {
            jvmsList.add(jvm);
        }
        application.setJvms(jvmsList);
        this.applications.add(application);
        applications.add(application);
        return applications;
    }

    public List<Application> getApplicationsMulti() {
        Group myGroup = new Group(groupId, groupName);
        List<Application> applications = new LinkedList<>();
        Application application = new Application(id(0L, Application.class),
                "SLPA-WS-4.0.0800.02",
                "myaWarPath",
                "/slpa-test/slum/ws",
                myGroup,
                true,
                false,
                false,
                "myWarName");
        List<Jvm> jvmsList = new LinkedList<>();
        for (Jvm jvm : jvms) {
            jvmsList.add(jvm);
        }
        application.setJvms(jvmsList);
        applications.add(application);
        return applications;
    }

    public List<Application> findApplications() {
        Group myGroup = new Group(groupId, groupName);
        List<Application> applications = new LinkedList<>();
        Application application = new Application(id(0L, Application.class),
                "HEALTH-CHECK-4.0",
                "myaWarPath",
                "/hct",
                myGroup,
                true,
                false,
                false,
                "myWarName");
        applications.add(application);
        return applications;
    }

    public List<WebServer> findWebServers() {
        Group myGroup = new Group(groupId, groupName);
        List<WebServer> webservers = new LinkedList<>();
        WebServer webServer = new WebServer(id(1L, WebServer.class),
                "localhost",
                "myWebServerName",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        WebServer webServer2 = new WebServer(id(1L, WebServer.class),
                "localhost2",
                "myWebServerName2",
                80,
                443,
                new Path("path"),
                new FileSystemPath("filesystempath"),
                new Path("svrRoot"),
                new Path("docRoot"),
                WebServerReachableState.WS_REACHABLE,
                "errorStatus",
                myGroup);
        webservers.add(webServer);
        webservers.add(webServer2);
        return webservers;
    }

    private Set<Group> groups = new HashSet<>();
    private Set<Jvm> jvms = new HashSet<>();
    private Set<Application> applications = new HashSet<>();
    private Set<WebServer> webServers = new HashSet<>();
}
