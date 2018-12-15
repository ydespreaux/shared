package com.ydespreaux.shared.data.elasticsearch.core.converter;

import com.ydespreaux.shared.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.junit.Test;
import org.springframework.data.mapping.context.MappingContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MappingElasticsearchConverterTest {

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToInitializeGivenMappingContextIsNull() {
		// given
		new MappingElasticsearchConverter(null);
	}

	@Test
	public void shouldReturnMappingContextWithWhichItWasInitialized() {
		// given
		MappingContext mappingContext = new SimpleElasticsearchMappingContext();
		MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);
		// then
		assertThat(converter.getMappingContext(), is(notNullValue()));
		assertThat(converter.getMappingContext(), is(sameInstance(mappingContext)));
	}
}
