package com.ydespreaux.shared.kafka.connect.runtime;

import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectProperties;
import com.ydespreaux.shared.kafka.connect.support.KafkaConnectTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.Herder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @since 1.0.0
 */
@Slf4j
public class ConnectEmbedded extends Connect {

    private final Herder herder;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch stopLatch = new CountDownLatch(1);
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final ConnectEmbedded.ShutdownHook shutdownHook;

    /**
     * Default constructor
     *
     * @param template
     * @param herder
     */
    public ConnectEmbedded(KafkaConnectTemplate template, Herder herder) {
        super(template);
        this.herder = herder;
        this.shutdownHook = new ConnectEmbedded.ShutdownHook();
    }

    /**
     * Start cluster.
     * Create or update the connectors
     * @param properties
     */
    @Override
    public void start(KafkaConnectProperties properties) {
        try {
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect starting");
            }
            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
            this.herder.start();
            if (log.isInfoEnabled()) {
                log.info("Kafka Connect started");
            }
            super.start(properties);
        } finally {
            this.startLatch.countDown();
        }
    }

    /**
     * Stop cluster
     */
    @Override
    public void stop() {
        try {
            boolean wasShuttingDown = this.shutdown.getAndSet(true);
            if (!wasShuttingDown) {
                if (log.isInfoEnabled()) {
                    log.info("Kafka Connect stopping");
                }
                this.herder.stop();
                if (log.isInfoEnabled()) {
                    log.info("Kafka Connect stopped");
                }
            }
        } finally {
            this.stopLatch.countDown();
        }
    }

    private class ShutdownHook extends Thread {
        private ShutdownHook() {
        }

        @Override
        public void run() {
            try {
                ConnectEmbedded.this.startLatch.await();
                ConnectEmbedded.this.stop();
            } catch (InterruptedException var2) {
                ConnectEmbedded.log.error("Interrupted in shutdown hook while waiting for Kafka Connect startup to finish");
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }

        }
    }
}
