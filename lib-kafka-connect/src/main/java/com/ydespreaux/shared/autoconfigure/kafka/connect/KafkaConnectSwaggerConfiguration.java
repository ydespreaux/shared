package com.ydespreaux.shared.autoconfigure.kafka.connect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.DescriptionResolver;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.function.Predicate;

import static com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectDataAutoConfiguration.TAG_CONNECTORS;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Swagger configuration
 *
 * @since 1.0.0
 */
@Slf4j
@Configuration
@PropertySource(value = {
        "classpath:/swagger/swagger-kafka-connect-messages_fr.properties"
})
public class KafkaConnectSwaggerConfiguration {

    @Value("${swagger.kafka-connect.group-name:kconnect}")
    private String groupName;

    /**
     * Resolver for swagger properties
     */
    private final DescriptionResolver resolver;
    /**
     * Kafka connect properties
     */
    private final KafkaConnectProperties properties;

    /**
     * Default constructor
     * @param resolver
     * @param properties
     */
    public KafkaConnectSwaggerConfiguration(DescriptionResolver resolver, KafkaConnectProperties properties) {
        this.resolver = resolver;
        this.properties = properties;
    }

    /**
     * Create default contact
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(Contact.class)
    public Contact createContact(){
        return new Contact(
                resolver.resolve("${swagger.kafka-connect.contact.name}"),
                resolver.resolve("${swagger.kafka-connect.contact.url}"),
                resolver.resolve("${swagger.kafka-connect.contact.email}"));
    }

    /**
     * Create the private api docket
     * @return
     */
    @Bean
    public Docket createKafkaConnectDocket(Contact contact) {
        String contextPath = this.properties.getContextPath();
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        return createDocket(groupName, PathSelectors.regex(contextPath+".*")::apply, createDefaultApiInfo(contact))
                .tags(createKafkaConnectTag());
    }

    /**
     * Create tag for kafka connect rest api
     * @return
     */
    private Tag createKafkaConnectTag(){
        return new Tag(TAG_CONNECTORS, resolver.resolve("${swagger.kafka-connect-rest-controller.description}"));
    }

    /**
     * Create a docket
     * @param groupName
     * @param predicateUrl
     * @return
     */
    private Docket createDocket(final String groupName, final Predicate<String> predicateUrl, ApiInfo info) {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(groupName)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ydespreaux.shared.kafka.connect.rest"))
                .paths(predicateUrl::test)
                .build()
                .apiInfo(info)
                .useDefaultResponseMessages(false);
    }

    /**
     *
     * @return
     */
    private ApiInfo createDefaultApiInfo(Contact contact){
        final ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title(resolver.resolve("${swagger.kafka-connect.title}"))
                .description(resolver.resolve("${swagger.kafka-connect.description}"))
                .license(resolver.resolve("${swagger.kafka-connect.license}"))
                .licenseUrl(resolver.resolve("${swagger.kafka-connect.license-url}"))
                .termsOfServiceUrl(resolver.resolve("${swagger.kafka-connect.terms-service-url}"))
                .version(resolver.resolve("${swagger.kafka-connect.version}"));
        if (contact != null && !isEmpty(contact.getName())){
            builder.contact(contact);
        }
        return builder.build();
    }
}
