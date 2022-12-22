package com.kolhozcustoms.WikiJS.config;

import com.kolhozcustoms.WikiJS.api.WikiJSComponent;
import com.kolhozcustoms.WikiJS.impl.WikiJSComponentImpl;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.ModuleFactoryBean;
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.exportOsgiService;
import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.importOsgiService;

@Configuration
@Import({
        ModuleFactoryBean.class,
        PluginAccessorBean.class
})
public class WikiJSJavaConfig {


    // imports ApplicationProperties from OSGi
    @Bean
    public ApplicationProperties applicationProperties() {
        return importOsgiService(ApplicationProperties.class);
    }

    @Bean
    public WikiJSComponent wikiJSComponent(ApplicationProperties applicationProperties) {
        return new WikiJSComponentImpl(applicationProperties);
    }

    // Exports WikiJSComponent as an OSGi service
    @Bean
    public FactoryBean<ServiceRegistration> registerMyDelegatingService(
            final WikiJSComponent wikiJSComponent) {
        return exportOsgiService(wikiJSComponent, null, WikiJSComponent.class);
    }
}