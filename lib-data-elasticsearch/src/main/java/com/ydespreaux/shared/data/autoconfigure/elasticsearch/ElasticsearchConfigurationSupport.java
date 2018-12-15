package com.ydespreaux.shared.data.autoconfigure.elasticsearch;

import com.ydespreaux.shared.data.autoconfigure.elasticsearch.settings.TemplateAction;
import com.ydespreaux.shared.data.autoconfigure.elasticsearch.settings.TemplateProperties;
import com.ydespreaux.shared.data.elasticsearch.core.DefaultEntityMapper;
import com.ydespreaux.shared.data.elasticsearch.core.DefaultResultsMapper;
import com.ydespreaux.shared.data.elasticsearch.core.EntityMapper;
import com.ydespreaux.shared.data.elasticsearch.core.ResultsMapper;
import com.ydespreaux.shared.data.elasticsearch.core.converter.ElasticsearchConverter;
import com.ydespreaux.shared.data.elasticsearch.core.converter.MappingElasticsearchConverter;
import com.ydespreaux.shared.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @since 1.1.0
 * @author yoann.despreaux
 */
@Slf4j
@Configuration
@AutoConfigureAfter(RestClientAutoConfiguration.class)
@EnableConfigurationProperties({JacksonProperties.class})
public class ElasticsearchConfigurationSupport implements ApplicationContextAware  {

    private ApplicationContext context;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    @Bean
    ElasticsearchConverter elasticsearchConverter() {
        return new MappingElasticsearchConverter(elasticsearchMappingContext());
    }

    @Bean
    @ConditionalOnMissingBean
    EntityMapper entityMapper(JacksonProperties jacksonPropetrties){
        return new DefaultEntityMapper(jacksonPropetrties);
    }

    @Bean
    @ConditionalOnMissingBean
    ResultsMapper resultsMapper(EntityMapper mapper, ElasticsearchConverter converter) {
        return new DefaultResultsMapper(mapper, converter);
    }

    /**
     * Creates a {@link SimpleElasticsearchMappingContext} equipped with entity classes scanned from the mapping base
     * package.
     *
     * @return never {@literal null}.
     */
    @Bean
    @SneakyThrows
    SimpleElasticsearchMappingContext elasticsearchMappingContext() {
        return new SimpleElasticsearchMappingContext();
    }

    /**
     * @return
     */
    protected List<TemplateSettings> buildTemplates(TemplateProperties templateProperties) {
        final List<TemplateSettings> templates = new ArrayList<>();
        if (templateProperties.getAction() == TemplateAction.NONE) {
            return templates;
        }
        final String[] profiles = context.getEnvironment().getActiveProfiles();
        templateProperties.getScripts().forEach(location -> {
            List<Resource> locations = new ArrayList<>(profiles.length + 1);
            Resource resource = context.getResource(location);
            if (resource.exists()) {
                locations.add(resource);
            } else if (log.isWarnEnabled()) {
                log.warn("Resource {} not found", location);
            }
            String extension = FilenameUtils.getExtension(location);
            boolean hasExtension = StringUtils.hasLength(extension);
            String prefix = location.substring(0, location.length() - (hasExtension ? extension.length() + 1 : 0));
            for (String profile : profiles) {
                String profilPath = prefix + "-" + profile + (hasExtension ? "." + extension : "");
                Resource profilResource = context.getResource(profilPath);
                if (profilResource.exists()) {
                    locations.add(profilResource);
                }
            }
            StringBuilder templateName = new StringBuilder(FilenameUtils.getBaseName(resource.getFilename()));
            if (profiles.length > 0) {
                templateName.append("-").append(profiles[0]);
            }
            templates.add(TemplateSettings.builder()
                    .action(templateProperties.getAction())
                    .name(templateName.toString())
                    .locations(locations)
                    .build());
        });
        return templates;
    }

    /**
     *
     * @since 1.0.0
     * @author yoann.despreaux
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateSettings {

        /**
         * Définit le nom du template
         */
        private String name;
        /**
         * Définit l'action à effectuer sur le template courant.
         */
        private TemplateAction action;
        /**
         * Définit la liste des ressources des scripts du template
         */
        private List<Resource> locations;

    }
}
