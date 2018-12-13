package com.ydespreaux.shared.data.autoconfigure.elasticsearch;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.settings.TemplateAction;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.settings.TemplateProperties;import com.ydespreaux.shared.data.elasticsearch.annotations.Document;import com.ydespreaux.shared.data.elasticsearch.core.*;import com.ydespreaux.shared.data.elasticsearch.core.converter.ElasticsearchConverter;import com.ydespreaux.shared.data.elasticsearch.core.converter.ElasticsearchCustomConversions;import com.ydespreaux.shared.data.elasticsearch.core.converter.MappingElasticsearchConverter;import com.ydespreaux.shared.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;import lombok.*;import lombok.extern.slf4j.Slf4j;import org.apache.commons.io.FilenameUtils;import org.elasticsearch.client.RestHighLevelClient;import org.springframework.beans.factory.config.BeanDefinition;import org.springframework.boot.autoconfigure.AutoConfigureAfter;import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;import org.springframework.boot.autoconfigure.jackson.JacksonProperties;import org.springframework.boot.context.properties.EnableConfigurationProperties;import org.springframework.context.ApplicationContext;import org.springframework.context.ApplicationContextAware;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;import org.springframework.context.annotation.Configuration;import org.springframework.core.convert.converter.Converter;import org.springframework.core.io.Resource;import org.springframework.core.type.filter.AnnotationTypeFilter;import org.springframework.data.annotation.Persistent;import org.springframework.util.ClassUtils;import org.springframework.util.StringUtils;import java.util.*;/** * Configuration providing beans for ElasticSearch operation. * Autoconfigure the use of lib-core-elasctic-jest module. */@Slf4j@Configuration@AutoConfigureAfter(RestClientAutoConfiguration.class)@EnableConfigurationProperties({TemplateProperties.class, JacksonProperties.class})public class ElasticsearchDataAutoConfiguration implements ApplicationContextAware {    private ApplicationContext context;    @Override    public void setApplicationContext(ApplicationContext applicationContext) {        this.context = applicationContext;    }    @Bean    public ElasticsearchConverter elasticsearchConverter() {        return new MappingElasticsearchConverter(elasticsearchMappingContext());    }    /**     * Creates a {@link SimpleElasticsearchMappingContext} equipped with entity classes scanned from the mapping base     * package.     *     * @see #getMappingBasePackages()     * @return never {@literal null}.     */    @Bean    @SneakyThrows    public SimpleElasticsearchMappingContext elasticsearchMappingContext() {        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();        mappingContext.setInitialEntitySet(getInitialEntitySet());        mappingContext.setSimpleTypeHolder(customConversions().getSimpleTypeHolder());        return mappingContext;    }    /**     * Register custom {@link Converter}s in a {@link ElasticsearchCustomConversions} object if required.     *     * @return never {@literal null}.     */    @Bean    public ElasticsearchCustomConversions customConversions() {        return new ElasticsearchCustomConversions(Collections.emptyList());    }    @Bean    @ConditionalOnMissingBean    EntityMapper entityMapper(JacksonProperties jacksonPropetrties){        return new DefaultEntityMapper(jacksonPropetrties);    }    @Bean    @ConditionalOnMissingBean    ResultsMapper resultsMapper(EntityMapper mapper, ElasticsearchConverter converter) {        return new DefaultResultsMapper(mapper, converter);    }    @Bean    @ConditionalOnMissingBean    public ElasticsearchOperations elasticsearchTemplate(            final RestHighLevelClient client,            final ElasticsearchConverter converter,            final ResultsMapper resultsMapper,            final TemplateProperties templateProperties) {        ElasticsearchTemplate operations = new ElasticsearchTemplate(client, converter, resultsMapper);        buildTemplates(templateProperties).forEach(template -> {            if (template.getAction() != TemplateAction.NONE) {                operations.createTemplate(template.getName(), template.getLocations(), template.getAction() == TemplateAction.CREATE_ONLY);            }        });        return operations;    }    /**     *     * @return     */    protected Collection<String> getMappingBasePackages() {        Package mappingBasePackage = getClass().getPackage();        return Collections.singleton(mappingBasePackage == null ? null : mappingBasePackage.getName());    }    /**     * Scans the mapping base package for classes annotated with {@link Document}. By default, it scans for entities in     * all packages returned by {@link #getMappingBasePackages()}.     *     * @see #getMappingBasePackages()     * @return never {@literal null}.     * @throws ClassNotFoundException     */    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {        Set<Class<?>> initialEntitySet = new HashSet<>();        for (String basePackage : getMappingBasePackages()) {            initialEntitySet.addAll(scanForEntities(basePackage));        }        return initialEntitySet;    }    /**     * Scans the given base package for entities, i.e. Elasticsearch specific types annotated with {@link Document} and     * {@link Persistent}.     *     * @param basePackage must not be {@literal null}.     * @return never {@literal null}.     * @throws ClassNotFoundException     */    protected Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {        if (!StringUtils.hasText(basePackage)) {            return Collections.emptySet();        }        Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();        if (StringUtils.hasText(basePackage)) {            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(                    false);            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));			for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {			    initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(),						ElasticsearchDataAutoConfiguration.class.getClassLoader()));			}        }        return initialEntitySet;    }    /**     * @return     */    protected List<TemplateSettings> buildTemplates(TemplateProperties templateProperties) {        final List<TemplateSettings> templates = new ArrayList<>();        if (templateProperties.getAction() == TemplateAction.NONE) {            return templates;        }        final String[] profiles = context.getEnvironment().getActiveProfiles();        templateProperties.getScripts().forEach(location -> {            List<Resource> locations = new ArrayList<>(profiles.length + 1);            Resource resource = context.getResource(location);            if (resource.exists()) {                locations.add(resource);            } else if (log.isWarnEnabled()) {                log.warn("Resource {} not found", location);            }            String extension = FilenameUtils.getExtension(location);            boolean hasExtension = StringUtils.hasLength(extension);            String prefix = location.substring(0, location.length() - (hasExtension ? extension.length() + 1 : 0));            for (String profile : profiles) {                String profilPath = prefix + "-" + profile + (hasExtension ? "." + extension : "");                Resource profilResource = context.getResource(profilPath);                if (profilResource.exists()) {                    locations.add(profilResource);                }            }            StringBuilder templateName = new StringBuilder(FilenameUtils.getBaseName(resource.getFilename()));            if (profiles.length > 0) {                templateName.append("-").append(profiles[0]);            }            templates.add(TemplateSettings.builder()                    .action(templateProperties.getAction())                    .name(templateName.toString())                    .locations(locations)                    .build());        });        return templates;    }    @Getter    @Setter    @NoArgsConstructor    @AllArgsConstructor    @Builder    public static class TemplateSettings {        /**         * Définit le nom du template         */        private String name;        /**         * Définit l'action à effectuer sur le template courant.         */        private TemplateAction action;        /**         * Définit la liste des ressources des scripts du template         */        private List<Resource> locations;    }}