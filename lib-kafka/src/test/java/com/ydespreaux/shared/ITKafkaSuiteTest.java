package com.ydespreaux.shared;

import com.ydespreaux.shared.autoconfigure.kafka.actuator.KafkaTopicHealthIndicatorUpTest;
import com.ydespreaux.shared.autoconfigure.kafka.actuator.RemoteConnectionHealthIndicatorTest;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaContainer;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    KafkaTopicHealthIndicatorUpTest.class,
    RemoteConnectionHealthIndicatorTest.class
})
public class ITKafkaSuiteTest {

    public static final String DEFAULT_GROUP = "groupe_junit";
    public static final String DEFAULT_TOPIC = "notifications";

    @ClassRule
    public static final ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer()
            .withTopic(DEFAULT_TOPIC, 1, false)
            .withSchemaRegistry(true)
            .withSchemaRegistrySystemProperty("spring.kafka.extra.schema-registry");


}
