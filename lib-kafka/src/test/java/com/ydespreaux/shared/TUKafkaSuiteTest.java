package com.ydespreaux.shared;

import com.ydespreaux.shared.autoconfigure.kafka.actuator.KafkaTopicHealthIndicatorDownTest;
import com.ydespreaux.shared.autoconfigure.kafka.actuator.MechanicKafkaStreamsHealthIndicatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    KafkaTopicHealthIndicatorDownTest.class,
    MechanicKafkaStreamsHealthIndicatorTest.class
})
public class TUKafkaSuiteTest {
}
