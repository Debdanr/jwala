package com.siemens.cto.aem.persistence.configuration;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.h2.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TestJpaConfiguration {

    @Bean
    public DataSource getDataSource() {
        return new SimpleDriverDataSource(new Driver(),
                                          "jdbc:h2:tcp://localhost/~/test",
                                          "sa",
                                          "");
    }

    @Bean
    public JpaVendorAdapter getJpaVendorAdapter() {
        return new OpenJpaVendorAdapter();
    }

    @Bean(name = "openJpaProperties")
    public Properties getJpaProperties() {
        final Properties properties = new Properties();
        properties.setProperty("org.apache.openjpa.jdbc.sql.DBDictionary", "org.apache.openjpa.jdbc.sql.H2Dictionary");
        return properties;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean() {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();

        factory.setJpaVendorAdapter(getJpaVendorAdapter());
        factory.setPersistenceXmlLocation("classpath:META-INF/aem-persistence.xml");
        factory.setDataSource(getDataSource());
        factory.setJpaProperties(getJpaProperties());

        return factory;
    }

    @Bean
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactoryBean().getObject();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager getTransactionManager() {
        final PlatformTransactionManager manager = new JpaTransactionManager(getEntityManagerFactory());
        return manager;
    }

    @Bean(name = "loadTimeWeaver")
    public LoadTimeWeaver getLoadTimeWeaver() {
        return new InstrumentationLoadTimeWeaver();
    }
}
