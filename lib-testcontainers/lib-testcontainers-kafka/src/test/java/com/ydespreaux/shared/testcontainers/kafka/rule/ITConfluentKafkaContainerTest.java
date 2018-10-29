package com.ydespreaux.shared.testcontainers.kafka.rule;


import com.ydespreaux.shared.testcontainers.kafka.domain.WorkstationAvro;
import com.ydespreaux.shared.testcontainers.kafka.utils.KafkaTestUtils;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@RunWith(SpringRunner.class)
public class ITConfluentKafkaContainerTest {

    @ClassRule
    public static final ConfluentKafkaContainer container = new ConfluentKafkaContainer()
            .withSchemaRegistry(true);

    @Test
    public void containerEnvironment() {
        assertThat(container.getBootstrapServers(), is(notNullValue()));
        assertThat(container.getZookeeperServer(), is(notNullValue()));
        assertThat(container.getSchemaRegistryServers(), is(notNullValue()));
    }

    @Test
    public void createTopic() {
        KafkaTestUtils.createTopic(container, "TOPIC1", 1, false);

        ZkUtils zkUtils = ZkUtils.apply(container.getZookeeperServer(), 6000, 6000, false);
        boolean exists = AdminUtils.topicExists(zkUtils, "TOPIC1");
        assertThat(exists, is(true));
    }

    @Test
    public void createCompactTopic() {
        KafkaTestUtils.createTopic(container, "TOPIC_COMPACT_1", 1, true);

        ZkUtils zkUtils = ZkUtils.apply(container.getZookeeperServer(), 6000, 6000, false);
        boolean exists = AdminUtils.topicExists(zkUtils, "TOPIC_COMPACT_1");
        assertThat(exists, is(true));
    }

    @Test
    public void produceMessage() throws InterruptedException {

        /**
         * List des messages envoyés avec succès
         */
        BlockingQueue<ProducerRecord<String, String>> records = new LinkedBlockingQueue<>();
        KafkaTestUtils.createTopic(container, "TOPIC2", 1, false);

        KafkaTemplate<String, String> template = KafkaTestUtils.createKafkaTemplate(container, new StringSerializer(), new StringSerializer());
        ListenableFuture<SendResult<String, String>> future = template.send("TOPIC2", "KEY", "Message : produceMessage()");
        future.addCallback(
                success -> {
                    log.info("Send message successfull : {}", success.toString());
                    records.add(success.getProducerRecord());
                },
                failed -> log.info("Send message failed : {}", failed.getCause())
        );

        ProducerRecord<String, String> record = records.poll(60, TimeUnit.SECONDS);
        assertThat(record, is(notNullValue()));
        assertThat(record.key(), is(equalTo("KEY")));
        assertThat(record.value(), is(equalTo("Message : produceMessage()")));
    }

    @Test
    public void consumeMessage() throws Exception {
        KafkaTestUtils.createTopic(container, "TOPIC_3", 1, false);

        /**
         * List des messages reçus
         */
        BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

        KafkaMessageListenerContainer<String, ?> listenerContainer = KafkaTestUtils.createListenerContainer(container,
                "an_junit_group",
                "TOPIC_3",
                new StringDeserializer(),
                new StringDeserializer(),
                record -> records.add(record));
        ContainerTestUtils.waitForAssignment(listenerContainer, 1);

        KafkaTemplate<String, String> template = KafkaTestUtils.createKafkaTemplate(container, new StringSerializer(), new StringSerializer());
        template.send("TOPIC_3", "KEY_1", "Message : consumeMessage()");

        ConsumerRecord<String, String> record = records.poll(60, TimeUnit.SECONDS);

        listenerContainer.stop();

        assertThat(record, is(notNullValue()));
        assertThat(record.key(), is(equalTo("KEY_1")));
        assertThat(record.value(), is(equalTo("Message : consumeMessage()")));


    }

    @Test
    public void produceMessageAvro() throws InterruptedException {

        /**
         * List des messages envoyés avec succès
         */
        BlockingQueue<ProducerRecord<String, WorkstationAvro>> records = new LinkedBlockingQueue<>();
        KafkaTestUtils.createTopic(container, "TOPIC2", 1, false);
        KafkaTemplate<String, WorkstationAvro> template = KafkaTestUtils.createKafkaTemplate(container);

        WorkstationAvro workstation = WorkstationAvro.newBuilder().setId(1L).setName("WS-123456").setSerialNumber("SERIAL-000001").build();

        ListenableFuture<SendResult<String, WorkstationAvro>> future = template.send("TOPIC_1", "WKS-1", workstation);
        future.addCallback(
                success -> {
                    log.info("Send message successfull : {}", success.toString());
                    records.add(success.getProducerRecord());
                },
                failed -> log.info("Send message failed : {}", failed.getCause())
        );
        ProducerRecord<String, WorkstationAvro> record = records.poll(60, TimeUnit.SECONDS);
        assertThat(record, is(notNullValue()));
        assertThat(record.key(), is(equalTo("WKS-1")));
        WorkstationAvro value = record.value();
        assertThat(value.getId(), is(equalTo(value.getId())));
        assertThat(value.getName(), is(equalTo(value.getName())));
        assertThat(value.getSerialNumber(), is(equalTo(value.getSerialNumber())));
    }

    @Test
    public void consumeMessageAvro() throws Exception {
        KafkaTestUtils.createTopic(container, "TOPIC_2", 1, false);

        /**
         * List des messages reçus
         */
        BlockingQueue<ConsumerRecord<String, WorkstationAvro>> records = new LinkedBlockingQueue<>();

        KafkaMessageListenerContainer<String, ?> listenerContainer = KafkaTestUtils.createListenerContainer(container,
                "an_junit_group",
                "TOPIC_2",
                (MessageListener<String, WorkstationAvro>) record -> records.add(record));
        ContainerTestUtils.waitForAssignment(listenerContainer, 1);

        KafkaTemplate<String, WorkstationAvro> template = KafkaTestUtils.createKafkaTemplate(container);
        WorkstationAvro workstation = WorkstationAvro.newBuilder().setId(2L).setName("WS-123456").setSerialNumber("SERIAL-000002").build();
        template.send("TOPIC_2", "WKS-2", workstation);

        ConsumerRecord<String, WorkstationAvro> record = records.poll(60, TimeUnit.SECONDS);

        listenerContainer.stop();

        assertThat(record, is(notNullValue()));
        assertThat(record.key(), is(equalTo("WKS-2")));
        WorkstationAvro value = record.value();
        assertThat(value.getId(), is(equalTo(value.getId())));
        assertThat(value.getName(), is(equalTo(value.getName())));
        assertThat(value.getSerialNumber(), is(equalTo(value.getSerialNumber())));

    }

}
