package com.siemens.cto.aem.persistence.jpa.domain;

import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;

import javax.persistence.*;

/**
 * JPA POJO for application resource template data.
 *
 * Created by z003bpej on 8/25/2015.
 */
@Entity
@Table(name = "APP_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"APP_ID", "TEMPLATE_NAME", "JVM_ID"})})
@NamedQueries({
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_RESOURCE_TEMPLATE_NAMES,
                query = "SELECT DISTINCT t.templateName FROM JpaApplicationConfigTemplate t WHERE t.app.name = :appName and t.jvm.name = :jvmName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaApplicationConfigTemplate t where t.app.name = :appName and t.templateName = :templateName and t.jvm = :templateJvm"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaApplicationConfigTemplate t where t.app.name = :appName and t.templateName = :templateName and t.jvm = :templateJvm"),
        @NamedQuery(name = JpaApplicationConfigTemplate.UPDATE_APP_TEMPLATE_CONTENT,
                query = "UPDATE JpaApplicationConfigTemplate t SET t.templateContent = :templateContent WHERE t.app.name = :appName AND t.templateName = :templateName and t.jvm = :templateJvm"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE,
                query = "SELECT t FROM JpaApplicationConfigTemplate t where t.templateName = :tempName and t.app.name = :appName and t.jvm.name = :jvmName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.QUERY_DELETE_APP_TEMPLATE, query = "DELETE FROM JpaApplicationConfigTemplate t WHERE t.templateName = :templateName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.GET_APP_TEMPLATE_RESOURCE_NAME,
                query = "SELECT t.templateName FROM JpaApplicationConfigTemplate t WHERE t.app.name = :appName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaApplicationConfigTemplate.QUERY_DELETE_APP_RESOURCE_BY_TEMPLATE_APP_JVM_NAME,
                query = "DELETE FROM JpaApplicationConfigTemplate t WHERE t.templateName = :templateName AND t.app.name = :appName AND t.jvm.name = :jvmName")
})
public class JpaApplicationConfigTemplate extends ConfigTemplate {

    public static final String GET_APP_RESOURCE_TEMPLATE_NAMES = "getAppResourceTemplateNames";
    public static final String GET_APP_TEMPLATE_CONTENT = "getAppTemplateContent";
    public static final String UPDATE_APP_TEMPLATE_CONTENT = "updateAppTemplateContent";
    public static final String GET_APP_TEMPLATE = "getAppTemplate";
    public static final String GET_APP_TEMPLATE_META_DATA = "getAppTemplateMetaData";
    public static final String QUERY_DELETE_APP_TEMPLATE = "deleteAppTemplate";
    public static final String QUERY_DELETE_APP_RESOURCE_BY_TEMPLATE_APP_JVM_NAME = "deleteAppResourceByTemplateJvmAppName";

    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final String QUERY_PARAM_JVM_NAME = "jvmName";
    public static final String QUERY_PARAM_APP_NAME = "appName";

    public static final String GET_APP_TEMPLATE_RESOURCE_NAME = "getAppTemplateResourceName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaApplication app;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaJvm jvm;

    public JpaApplication getApplication() {
        return app;
    }

    public void setApplication(final JpaApplication app) {
        this.app = app;
    }

    public JpaJvm getJvm() {
        return jvm;
    }

    public void setJvm(JpaJvm jvm) {
        this.jvm = jvm;
    }
}
