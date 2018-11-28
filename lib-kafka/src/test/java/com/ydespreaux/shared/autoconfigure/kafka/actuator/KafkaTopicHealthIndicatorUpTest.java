package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import com.ydespreaux.shared.autoconfigure.kafka.KafkaExtraProperties;
import com.ydespreaux.shared.commons.io.support.YamlPropertySourceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource(factory = YamlPropertySourceFactory.class, value = "classpath:kafka-test.yml")
@EnableConfigurationProperties(KafkaExtraProperties.class)
public class KafkaTopicHealthIndicatorUpTest {

    @Autowired
    private KafkaExtraProperties properties;

    @Test
    public void doHealthCheck() throws Exception {
        KafkaHealthIndicatorAutoConfiguration.KafkaClientHealthIndicator indicator = new KafkaHealthIndicatorAutoConfiguration.KafkaClientHealthIndicator(this.properties.getKafkaProperties());
        Health health = indicator.health();
        assertThat(health.getStatus(), is(equalTo(Status.UP)));
    }

}
