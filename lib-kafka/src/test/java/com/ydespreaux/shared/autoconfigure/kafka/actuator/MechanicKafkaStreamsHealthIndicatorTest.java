package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import org.apache.kafka.streams.KafkaStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class MechanicKafkaStreamsHealthIndicatorTest {

    @Test
    public void nullStreamsTest() {
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(null);
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.UP)));
    }

    @Test
    public void streamsRunningTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.UP)));
    }

    @Test
    public void streamsErrorTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.ERROR);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.DOWN)));
    }

    @Test
    public void streamsCreatedTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.CREATED);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.DOWN)));
    }

    @Test
    public void streamsNotRunningTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.NOT_RUNNING);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.DOWN)));
    }

    @Test
    public void streamsPendingShutdownTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.PENDING_SHUTDOWN);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.DOWN)));
    }

    @Test
    public void streamsRebalancingTest() {
        KafkaStreams runningStreams = mock(KafkaStreams.class);
        when(runningStreams.state()).thenReturn(KafkaStreams.State.REBALANCING);
        KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator healthIndicator = new KafkaStreamHealthIndicatorAutoConfiguration.MechanicKafkaStreamsHealthIndicator(Collections.singletonList(runningStreams));
        assertThat(healthIndicator.health().getStatus(), is(equalTo(Status.DOWN)));
    }

}
