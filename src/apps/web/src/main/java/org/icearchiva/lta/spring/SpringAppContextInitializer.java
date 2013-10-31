package org.icearchiva.lta.spring;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.context.ConfigurableWebApplicationContext;

public class SpringAppContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {
    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        try {
            propertySources.addFirst(new ResourcePropertySource("classpath:/spring-profiles.properties"));
        } catch (Exception e) {}
    }
 
}
