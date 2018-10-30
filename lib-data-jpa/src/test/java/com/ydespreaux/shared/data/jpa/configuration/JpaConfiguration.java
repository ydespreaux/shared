/**
 *
 */
package com.ydespreaux.shared.data.jpa.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Jpa configuration
 *
 * @author xpax624
 */
@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.ydespreaux.shared.data.jpa.configuration.entities"})
@EnableJpaRepositories(basePackages = {"com.ydespreaux.shared.data.jpa.configuration.repository"})
public class JpaConfiguration {

}
