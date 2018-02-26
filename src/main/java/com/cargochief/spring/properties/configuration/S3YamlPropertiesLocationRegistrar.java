package com.cargochief.spring.properties.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.SystemPropertyUtils;

import com.spring.loader.util.SystemPropertyResolver;
import com.spring.loader.util.WordUtils;

import com.cargochief.spring.properties.S3YamlPropertiesLocation;

public class S3YamlPropertiesLocationRegistrar
        implements EnvironmentAware, ImportBeanDefinitionRegistrar {

    private Environment environment;

    public S3YamlPropertiesLocationRegistrar() {}

    public S3YamlPropertiesLocationRegistrar(Environment environment,
            SystemPropertyResolver resolver) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata,
            BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata
                .getAnnotationAttributes(S3YamlPropertiesLocation.class.getName()));
        String[] profiles = attributes.getStringArray("profiles");

        if (profiles.length > 0 && !environment.acceptsProfiles(profiles)) {
            return;
        }

        String[] locations = attributes.getStringArray("value");

        String[] formattedLocations = new String[locations.length];

        for (int i = 0; i < locations.length; i++) {
            formattedLocations[i] = SystemPropertyUtils.resolvePlaceholders(locations[i]);
        }

        BeanDefinition configurerDefinition =
                new RootBeanDefinition(S3YamlPropertiesSourceConfigurer.class);
        configurerDefinition.getPropertyValues().addPropertyValue("s3ResourceLoader",
                new RuntimeBeanReference("s3ResourceLoader"));
        configurerDefinition.getPropertyValues().add("locations", formattedLocations);

        registry.registerBeanDefinition(
                WordUtils.classNameloweredCaseFirstLetter(S3YamlPropertiesSourceConfigurer.class),
                configurerDefinition);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
