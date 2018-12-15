package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import com.ydespreaux.shared.data.elasticsearch.annotations.Score;
import org.junit.Test;
import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SimpleElasticsearchPersistentEntityTest {

	@Test(expected = MappingException.class)
	public void shouldThrowExceptionGivenVersionPropertyIsNotLong() throws NoSuchFieldException, IntrospectionException {
		// given
		TypeInformation typeInformation = ClassTypeInformation.from(EntityWithWrongVersionType.class);
		SimpleElasticsearchPersistentEntity<EntityWithWrongVersionType> entity = new SimpleElasticsearchPersistentEntity<>(
				typeInformation);

		SimpleElasticsearchPersistentProperty persistentProperty = createProperty(entity, "version");

		// when
		entity.addPersistentProperty(persistentProperty);
	}

	@Test(expected = MappingException.class)
	public void shouldThrowExceptionGivenMultipleVersionPropertiesArePresent()
			throws NoSuchFieldException, IntrospectionException {
		// given
		TypeInformation typeInformation = ClassTypeInformation.from(EntityWithMultipleVersionField.class);
		SimpleElasticsearchPersistentEntity<EntityWithMultipleVersionField> entity = new SimpleElasticsearchPersistentEntity<>(
				typeInformation);

		SimpleElasticsearchPersistentProperty persistentProperty1 = createProperty(entity, "version1");

		SimpleElasticsearchPersistentProperty persistentProperty2 = createProperty(entity, "version2");

		entity.addPersistentProperty(persistentProperty1);
		// when
		entity.addPersistentProperty(persistentProperty2);
	}
	
	@Test // DATAES-462
	public void rejectsMultipleScoreProperties() {

		SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();

		assertThatExceptionOfType(MappingException.class) //
				.isThrownBy(() -> context.getRequiredPersistentEntity(TwoScoreProperties.class)) //
				.withMessageContaining("first") //
				.withMessageContaining("second");
	}

	private static SimpleElasticsearchPersistentProperty createProperty(SimpleElasticsearchPersistentEntity<?> entity,
			String field) {

		TypeInformation<?> type = entity.getTypeInformation();
		Property property = Property.of(type, ReflectionUtils.findField(entity.getType(), field));
		return new SimpleElasticsearchPersistentProperty(property, entity, SimpleTypeHolder.DEFAULT);

	}

	private class EntityWithWrongVersionType {

		@Version
        private String version;

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
	}

	private class EntityWithMultipleVersionField {

		@Version
        private Long version1;
		@Version
        private Long version2;

		public Long getVersion1() {
			return version1;
		}

		public void setVersion1(Long version1) {
			this.version1 = version1;
		}

		public Long getVersion2() {
			return version2;
		}

		public void setVersion2(Long version2) {
			this.version2 = version2;
		}
	}

	// DATAES-462
	
	static class TwoScoreProperties {
		
		@Score float first;
		@Score float second;
	}
}
