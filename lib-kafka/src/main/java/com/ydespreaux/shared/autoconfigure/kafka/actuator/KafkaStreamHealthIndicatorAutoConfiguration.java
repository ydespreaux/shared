package com.ydespreaux.shared.autoconfigure.kafka.actuator;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureBefore({ EndpointAutoConfiguration.class })
public class KafkaStreamHealthIndicatorAutoConfiguration {

	@Configuration
	@ConditionalOnBean(KafkaStreams.class)
	@ConditionalOnEnabledHealthIndicator("kafka-stream")
	public static class KafkaStreamHealthIndicatorConfiguration {

		@Autowired(required = false)
		private List<KafkaStreams> kafkaStreams;

		@Bean
		public HealthIndicator kafkaStreams() {
		    return new MechanicKafkaStreamsHealthIndicator(kafkaStreams);
		}

	}

    public static class MechanicKafkaStreamsHealthIndicator extends AbstractHealthIndicator {

        private final List<KafkaStreams> kafkaStreams;

        @Autowired
        public MechanicKafkaStreamsHealthIndicator(List<KafkaStreams> kafkaStreams) {
            this.kafkaStreams = kafkaStreams;
        }

        @Override
        protected void doHealthCheck(Health.Builder builder) throws Exception {
            boolean isUp = true;
            if (this.kafkaStreams != null) {
                for (KafkaStreams streams : this.kafkaStreams) {
                    if (!isUpState(streams.state())) {
                        isUp = false;
                        break;
                    }
                }
            }
            if (isUp) {
                builder.up();
            }else {
                builder.down();
            }
        }

        private boolean isUpState(KafkaStreams.State stateToCompare) {
            return KafkaStreams.State.RUNNING == stateToCompare;
        }
    }

}
