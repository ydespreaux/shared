package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Elasticsearch specific {@link org.springframework.data.mapping.PersistentProperty} implementation processing
 *
 */
public class SimpleElasticsearchPersistentProperty extends
        AnnotationBasedPersistentProperty<ElasticsearchPersistentProperty> implements ElasticsearchPersistentProperty {

	private static final List<String> SUPPORTED_ID_PROPERTY_NAMES = Arrays.asList("id", "document");

	private final boolean isScore;
	private final boolean isParent;
	private final boolean isId;

	public SimpleElasticsearchPersistentProperty(Property property,
												 PersistentEntity<?, ElasticsearchPersistentProperty> owner,
												 SimpleTypeHolder simpleTypeHolder) {

		super(property, owner, simpleTypeHolder);

		this.isId = super.isIdProperty() || SUPPORTED_ID_PROPERTY_NAMES.contains(getFieldName());
//		this.isScore = isAnnotationPresent(Score.class);
//		this.isParent = isAnnotationPresent(Parent.class);
		this.isScore = false;
		this.isParent = false;

		if (isVersionProperty() && getType() != Long.class) {
			throw new MappingException(String.format("Version property %s must be of type Long!", property.getName()));
		}

		if (isScore && !Arrays.asList(Float.TYPE, Float.class).contains(getType())) {
			throw new MappingException(
					String.format("Score property %s must be either of type float or Float!", property.getName()));
		}

		if (isParent && getType() != String.class) {
			throw new MappingException(String.format("Parent property %s must be of type String!", property.getName()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#getFieldName()
	 */
	@Override
	public String getFieldName() {
		return getProperty().getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.AnnotationBasedPersistentProperty#isIdProperty()
	 */
	@Override
	public boolean isIdProperty() {
		return isId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.AbstractPersistentProperty#createAssociation()
	 */
	@Override
	protected Association<ElasticsearchPersistentProperty> createAssociation() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isScoreProperty()
	 */
	@Override
	public boolean isScoreProperty() {
		return isScore;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.AbstractPersistentProperty#isImmutable()
	 */
	@Override
	public boolean isImmutable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty#isParentProperty()
	 */
	@Override
	public boolean isParentProperty() {
		return isParent;
	}
}
