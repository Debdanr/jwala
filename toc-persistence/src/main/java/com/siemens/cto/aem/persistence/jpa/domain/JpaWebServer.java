package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "webserver", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
@NamedQueries({
    @NamedQuery(name = JpaWebServer.FIND_WEB_SERVER_BY_QUERY,
                query ="SELECT ws FROM JpaWebServer ws WHERE ws.name = :wsName"),
    @NamedQuery(name = JpaWebServer.FIND_JVMS_QUERY,
                query = "SELECT DISTINCT jvm FROM JpaJvm jvm JOIN jvm.groups g " +
                        "WHERE g.id IN (SELECT a.group FROM JpaApplication a " +
                        "WHERE a.group IN (:groups))"),
    @NamedQuery(name = JpaWebServer.QUERY_UPDATE_STATE_BY_ID, query = "UPDATE JpaWebServer w SET w.state = :state WHERE w.id = :id"),
    @NamedQuery(name = JpaWebServer.QUERY_UPDATE_ERROR_STATUS_BY_ID, query = "UPDATE JpaWebServer w SET w.errorStatus = :errorStatus WHERE w.id = :id"),
    @NamedQuery(name = JpaWebServer.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID, query = "UPDATE JpaWebServer w SET w.state = :state, w.errorStatus = :errorStatus WHERE w.id = :id"),
    @NamedQuery(name = JpaWebServer.QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaWebServer w WHERE w.state = :state AND w.groups.name = :groupName"),
    @NamedQuery(name = JpaWebServer.QUERY_GET_WS_COUNT_BY_GROUP_NAME, query = "SELECT COUNT(1) FROM JpaWebServer w WHERE w.groups.name = :groupName"),
    @NamedQuery(name = JpaWebServer.QUERY_GET_WS_AND_ITS_GROUPS, query = "SELECT w FROM JpaWebServer w LEFT JOIN FETCH w.groups WHERE w.id = :id")
})
public class JpaWebServer extends AbstractEntity<JpaWebServer> {

    private static final long serialVersionUID = 1L;
    public static final String WEB_SERVER_PARAM_NAME = "wsName";
    public static final String FIND_WEB_SERVER_BY_QUERY = "findWebServerByNameQuery";
    public static final String FIND_JVMS_QUERY = "findJvmsQuery";

    public static final String QUERY_UPDATE_STATE_BY_ID = "updateWebServerStateById";
    public static final String QUERY_UPDATE_ERROR_STATUS_BY_ID = "updateWebServerErrorStatusById";
    public static final String QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID = "updateWebServerStateAndErrStsById";
    public static final String QUERY_GET_WS_COUNT_BY_STATE_AND_GROUP_NAME = "getWebServerCountByStateAndGroupName";
    public static final String QUERY_GET_WS_COUNT_BY_GROUP_NAME = "getWebServerCountByGroupName";
    public static final java.lang.String QUERY_GET_WS_AND_ITS_GROUPS = "getWebServerAndItsGroups";

    public static final String QUERY_PARAM_ID = "id";
    public static final String QUERY_PARAM_STATE = "state";
    public static final String QUERY_PARAM_ERROR_STATUS = "errorStatus";
    public static final String QUERY_PARAM_GROUP_NAME = "groupName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String host;

    private String name;

    private Integer port;

    private Integer httpsPort;

    @Column(nullable = false)
    private String statusPath;

    @Column(nullable = false)
    private String httpConfigFile;

    @Column(nullable = false)
    private String svrRoot;

    @Column(nullable = false)
    private String docRoot;

    @Enumerated(EnumType.STRING)
    private WebServerReachableState state;

    @Column(name = "ERR_STS", length = 2147483647)
    private String errorStatus = "";

    @ManyToMany(mappedBy = "webServers", fetch = FetchType.EAGER)
    private List<JpaGroup> groups = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<JpaGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<JpaGroup> newGroups) {
        groups = newGroups;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getStatusPath() {
        return statusPath;
    }

    public void setStatusPath(final String statusPath) {
        this.statusPath = statusPath;
    }

    public String getHttpConfigFile() {
        return httpConfigFile;
    }

    public void setHttpConfigFile(String httpConfigFile) {
        this.httpConfigFile = httpConfigFile;
    }

    public String getSvrRoot() {
        return svrRoot;
    }

    public void setSvrRoot(String svrRoot) {
        this.svrRoot = svrRoot;
    }

    public String getDocRoot() {
        return docRoot;
    }

    public void setDocRoot(String docRoot) {
        this.docRoot = docRoot;
    }

    public WebServerReachableState getState() {
        return state;
    }

    public void setState(WebServerReachableState state) {
        this.state = state;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    @Override
    protected void prePersist() {
        super.prePersist();
        if (state == null) {
            state = WebServerReachableState.WS_UNREACHABLE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JpaWebServer that = (JpaWebServer) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JpaWebServer{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", httpsPort=" + httpsPort +
                ", statusPath='" + statusPath + '\'' +
                ", httpConfigFile='" + httpConfigFile + '\'' +
                ", svrRoot='" + svrRoot + '\'' +
                ", docRoot='" + docRoot + '\'' +
                '}';
    }

}
