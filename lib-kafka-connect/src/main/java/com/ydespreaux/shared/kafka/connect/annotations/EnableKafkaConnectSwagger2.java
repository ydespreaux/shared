package com.ydespreaux.shared.kafka.connect.annotations;

import com.ydespreaux.shared.autoconfigure.kafka.connect.KafkaConnectSwaggerConfiguration;
import org.springframework.context.annotation.Import;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.*;

/**
 * Enable swagger interface.
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableSwagger2
@Import(KafkaConnectSwaggerConfiguration.class)
public @interface EnableKafkaConnectSwagger2 {
}
