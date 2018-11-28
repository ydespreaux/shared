package com.ydespreaux.shared;

import com.ydespreaux.shared.autoconfigure.kafka.KafkaExtraPropertiesTest;
import com.ydespreaux.shared.autoconfigure.kafka.actuator.KafkaTopicHealthIndicatorDownTest;
import com.ydespreaux.shared.autoconfigure.kafka.actuator.MechanicKafkaStreamsHealthIndicatorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    KafkaExtraPropertiesTest.class,
    KafkaTopicHealthIndicatorDownTest.class,
    MechanicKafkaStreamsHealthIndicatorTest.class
})
public class TUKafkaSuiteTest {
}
