package com.siemens.cto.aem.persistence.jpa.domain.resource.config.template;

import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about a group of web server related resource configuration templates.
 */
@Entity
@Table(name = "GRP_WEBSERVER_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupWebServerConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupWebServerConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaGroupWebServerConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupWebServerConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.QUERY_DELETE_GRP_WEBSERVER_TEMPLATE, query = "DELETE FROM JpaGroupWebServerConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.QUERY_DELETE_GROUP_WEBSERVER_TEMPLATE_BY_GROUP_NAME, query = "DELETE FROM JpaGroupWebServerConfigTemplate t WHERE t.templateName = :templateName AND t.jpaGroup.name = :groupName"),
        @NamedQuery(name = JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAME, query = "SELECT t.templateName FROM JpaGroupWebServerConfigTemplate t WHERE t.grp.name = :groupName and t.templateName = :templateName")
})
public class JpaGroupWebServerConfigTemplate extends ConfigTemplate {
    public static final String GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES = "getGroupWebServerTemplateResourcesName";
    public static final String GET_GROUP_WEBSERVER_TEMPLATE_CONTENT = "getGroupWebServerTemplateContent";
    public static final String UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT = "updateGroupWebServerTemplateContent";
    public static final String GET_GROUP_WEBSERVER_TEMPLATE_META_DATA = "getGroupWebServerTemplateMetaData";
    public static final String QUERY_DELETE_GRP_WEBSERVER_TEMPLATE = "deleteGrpWebServerTemplate";

    public static final String QUERY_DELETE_GROUP_WEBSERVER_TEMPLATE_BY_GROUP_NAME = "deleteGroupWebServerTemplateByGroupName";
    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_GROUP_NAME = "groupName";

    public static final String GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAME = "getGroupWebServerTemplateResourceName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    public JpaGroup getJpaGroup() {
        return grp;
    }

    public void setJpaGroup(JpaGroup jpaGroup) {
        this.grp = jpaGroup;
    }
}
