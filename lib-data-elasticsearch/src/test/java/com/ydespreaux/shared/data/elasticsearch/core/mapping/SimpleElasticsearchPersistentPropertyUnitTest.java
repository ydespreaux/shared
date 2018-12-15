package com.ydespreaux.shared.data.elasticsearch.core.mapping;

import com.ydespreaux.shared.data.elasticsearch.annotations.Score;
import org.junit.Test;
import org.springframework.data.mapping.MappingException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SimpleElasticsearchPersistentPropertyUnitTest {

	@Test // DATAES-462
	public void rejectsScorePropertyOfTypeOtherthanFloat() {

		SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();

		assertThatExceptionOfType(MappingException.class) //
				.isThrownBy(() -> context.getRequiredPersistentEntity(InvalidScoreProperty.class)) //
				.withMessageContaining("scoreProperty");
	}

	static class InvalidScoreProperty {
		@Score
		String scoreProperty;
	}
}
