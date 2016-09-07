package com.cerner.jwala.persistence.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.cerner.jwala.common.exception.ApplicationException;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class AemJpaConfiguration {

    @Bean
    public DataSource getAemDataSource() {
        try {
            return JndiLocatorDelegate.createDefaultResourceRefLocator().lookup("jdbc/toc-xa",
                    DataSource.class);
        } catch (final NamingException ne) {
            throw new ApplicationException(ne);
        }
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean(final DataSource dataSource) {

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(new OpenJpaVendorAdapter());
        factory.setPersistenceUnitName("jwala-unit");
        factory.setPersistenceXmlLocation("classpath:META-INF/persistence.xml");

        return factory;
    }

    @Bean
    public PlatformTransactionManager getPlatformTransactionManager(final EntityManagerFactory emf) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDefaultTimeout(30);
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

}
