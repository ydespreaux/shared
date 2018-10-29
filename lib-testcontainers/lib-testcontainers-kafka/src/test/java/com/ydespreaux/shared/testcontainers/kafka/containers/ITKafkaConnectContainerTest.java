package com.ydespreaux.shared.testcontainers.kafka.containers;

import com.ydespreaux.shared.testcontainers.kafka.domain.WorkerInfo;
import com.ydespreaux.shared.testcontainers.kafka.rule.ConfluentKafkaContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
public class ITKafkaConnectContainerTest {

    @ClassRule
    public static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer()
            .withRegisterSpringbootProperties(false);

    private static KafkaConnectContainer kafkaConnectContainer;


    @BeforeClass
    public static void onSetupClass(){
        kafkaConnectContainer = new KafkaConnectContainer<>("4.1.0",
                kafkaContainer.getKafkaContainer().getInternalURL())
            .withNetwork(kafkaContainer.getNetwork());
        kafkaConnectContainer.start();
    }

    @AfterClass
    public static void onTeardownClass(){
        if (kafkaConnectContainer != null) {
            kafkaConnectContainer.stop();
        }
    }

    @Test
    public void containerEnvironment(){
        assertThat(kafkaConnectContainer.getURL(), is(notNullValue()));
    }

    @Test
    public void checkAppRest(){
        RestTemplateBuilder builder = new RestTemplateBuilder();
        builder = builder.rootUri(kafkaConnectContainer.getURL());
        RestTemplate template = builder.build();

        WorkerInfo info = template.getForObject("/", WorkerInfo.class);
        assertThat(info, is(notNullValue()));
        assertThat(info.getVersion(), is(notNullValue()));
        assertThat(info.getCommit(), is(notNullValue()));
    }

}
