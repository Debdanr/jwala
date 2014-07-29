package com.siemens.cto.aem.control.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemSshConfig.class,
         AemCommandExecutorConfig.class})
public class AemControlConfigReference {
}
