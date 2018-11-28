package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.ydespreaux.shared.ITKafkaSuiteTest.kafkaContainer;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
public class RemoteConnectionHealthIndicatorTest {

	private static final String HOST = "localhost";
//		private static final int PORT = 9090;

	@Test
	public void emptyConnectionTest() {
		final List<String> connections = new ArrayList<>();
		final RemoteConnectionHealthIndicator healthIndicator = new RemoteConnectionHealthIndicator(connections);

		final Health health = healthIndicator.health();

		assertThat(health, is(not(nullValue())));
		assertThat(health.getStatus(), is(equalTo(Status.DOWN)));
	}

	@Test
	public void nullConnectionTest() {
		final List<String> connections = null;
		final RemoteConnectionHealthIndicator healthIndicator = new RemoteConnectionHealthIndicator(connections);

		final Health health = healthIndicator.health();

		assertThat(health, is(not(nullValue())));
		assertThat(health.getStatus(), is(equalTo(Status.DOWN)));
	}

	@Test
	public void downHealthIndicatorTest() {
		final RemoteConnectionHealthIndicator healthIndicator = new RemoteConnectionHealthIndicator("randomHost:" + 0);

		final Health health = healthIndicator.health();

		assertThat(health, is(not(nullValue())));
		assertThat(health.getStatus(), is(equalTo(Status.DOWN)));
	}

	@Test
	public void upHealthIndicatorTest() throws InterruptedException {
		with().pollDelay(3, TimeUnit.SECONDS).await().atMost(5, TimeUnit.SECONDS).until(this.stubCallable());
		final RemoteConnectionHealthIndicator healthIndicator = new RemoteConnectionHealthIndicator(HOST + ":" + kafkaContainer.getSchemaRegistryContainer().getMappingPort());
		final Health health = healthIndicator.health();
		assertThat(health, is(not(nullValue())));
		assertThat(health.getStatus(), is(equalTo(Status.UP)));
	}

	@Test
	public void mixedHealthIndicatorTest() throws InterruptedException {

		with().pollDelay(3, TimeUnit.SECONDS).await().atMost(5, TimeUnit.SECONDS).until(this.stubCallable());

		final String hostWithoutPort = "hostWithoutPort";
		final String hostWithMultiplePoint = "host:with:points";
		final String hostWithBadPort = "host:80p";

		final List<String> connections = new ArrayList<>();
		connections.add(HOST + ":" + kafkaContainer.getSchemaRegistryContainer().getMappingPort());
		connections.add(hostWithoutPort);
		connections.add(hostWithMultiplePoint);
		connections.add(hostWithBadPort);

		final RemoteConnectionHealthIndicator healthIndicator = new RemoteConnectionHealthIndicator(connections);

		final Health health = healthIndicator.health();
		for (Map.Entry<String, Object> details : health.getDetails().entrySet()) {
			switch (details.getKey()) {
				case HOST:
					assertThat(details.getValue(), is(equalTo(Status.UP.getCode())));
					break;

				case hostWithoutPort:
				case hostWithMultiplePoint:
					assertThat(details.getValue(), is(equalTo(Status.DOWN.getCode())));
					break;

			}
		}

	}

	private Callable<Boolean> stubCallable() {
		return () -> true;
	}
}
