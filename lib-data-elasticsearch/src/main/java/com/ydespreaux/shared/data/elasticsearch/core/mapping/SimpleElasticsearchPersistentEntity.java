package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import com.ydespreaux.shared.data.elasticsearch.annotations.Document;
import com.ydespreaux.shared.data.elasticsearch.core.IndexTimeBasedParameter;
import com.ydespreaux.shared.data.elasticsearch.core.IndexTimeBasedSupport;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class SimpleElasticsearchPersistentEntity<T> extends BasicPersistentEntity<T, ElasticsearchPersistentProperty> implements ElasticsearchPersistentEntity<T>, ApplicationContextAware {

    private static final Pattern pattern = Pattern.compile("\\Q${\\E(.+?)\\Q}\\E");

    private ApplicationContext context;
    private Class<T> entityClass;
    private String aliasOrIndexName;
    private String indexPattern;
    private String typeName;
    private IndexTimeBasedSupport<T> indexSupport;
    private Boolean createIndex;
    private Boolean indexTimeBased;
    private String indexPath;

    /**
     *
     * @param typeInformation
     */
    public SimpleElasticsearchPersistentEntity(TypeInformation<T> typeInformation) {
        super(typeInformation);
    }

    /**
     *
     */
    private void afterPropertiesSet(){
        this.entityClass = this.getTypeInformation().getType();
        final Document document = this.entityClass.getAnnotation(Document.class);
        this.typeName = document.type();
        this.createIndex = document.createIndex;

        Environment env = context.getEnvironment();
        this.aliasOrIndexName = getEnvironmentValue(env, document.aliasOrIndex());
        this.indexPattern = getEnvironmentValue(env, document.indexPattern());
        this.indexTimeBased = StringUtils.hasText(this.indexPattern);
        this.indexPath = document.indexPath();
        try {
            this.indexSupport = document.indexTimeBasedSupport().newInstance();
        } catch (Exception e) {
            throw new ElasticsearchException(e);
        }

//        List<Field> fields = FieldUtils.getAllFieldsList(this.entityClass);
//        for (Field field : fields) {
//            if (field.isAnnotationPresent(Id.class)) {
//                field.setAccessible(true);
//                this.idProperty = field;
//            } else if (field.isAnnotationPresent(Version.class)) {
//                field.setAccessible(true);
//                this.versionProperty = field;
//            }
//            if (this.idProperty != null && this.versionProperty != null) {
//                break;
//            }
//        }

    }

    /**
     * @param source
     * @return
     */
    @Override
    public String getIndex(T source) {
        if (isIndexTimeBased()) {
            return this.indexSupport.buildIndex(IndexTimeBasedParameter.of(indexPattern, new Date(), source));
        }
        return this.aliasOrIndexName;
    }

    @Override
    public void setPersistentEntityId(T entity, String id) {
        ElasticsearchPersistentProperty idProperty = getIdProperty();
        if (idProperty == null) {
            if (log.isWarnEnabled()) {
                log.warn("No propertyId defined for entity class {}", entityClass);
            }
            return;
        }
        getPropertyAccessor(entity).setProperty(idProperty, id);
    }

    /**
     * @param entity
     * @param version
     */
    @Override
    public void setPersistentEntityVersion(T entity, Long version) {
        ElasticsearchPersistentProperty versionProperty = getVersionProperty();
        if (versionProperty != null) {
            getPropertyAccessor(entity).setProperty(versionProperty, version);
        }
    }

    /**
     * @param source
     * @return
     */
    @Override
    public String getPersistentEntityId(T source) {
        ElasticsearchPersistentProperty idProperty = getIdProperty();
        if (idProperty == null){
            if (log.isWarnEnabled()) {
                log.warn("No propertyId defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return (String) getPropertyAccessor(source).getProperty(idProperty);
        } catch (Exception e) {
            throw new IllegalStateException("failed to load id field", e);
        }
    }

    /**
     * @param source
     * @return
     */
    @Override
    public Long getPersistentEntityVersion(T source) {
        ElasticsearchPersistentProperty versionProperty = getVersionProperty();
        if (versionProperty == null){
            if (log.isWarnEnabled()) {
                log.warn("No version defined for entity class {}", entityClass);
            }
            return null;
        }
        try {
            return (Long) getPropertyAccessor(source).getProperty(versionProperty);
        } catch (Exception e) {
            throw new IllegalStateException("failed to load version field", e);
        }
    }

    /**
     * @return
     */
    @Override
    public Boolean createIndex() {
        return this.createIndex;
    }

    /**
     * @return
     */
    @Override
    public Boolean isIndexTimeBased() {
        return this.indexTimeBased;
    }

    /**
     * @return
     */
    @Override
    public String getIndexPath() {
        return this.indexPath;
    }

    /**
     *
     * @param environment
     * @param expression
     * @return
     */
    private String getEnvironmentValue(Environment environment, String expression) {
        String value = null;
        // Create the matcher
        Matcher matcher = pattern.matcher(expression);
        // If the matching is there, then add it to the map and return the value
        if (matcher.find()) {
            value = environment.getProperty(matcher.group(1));
        }
        return value == null ? expression : value;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        afterPropertiesSet();
    }

}