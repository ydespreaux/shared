package com.ydespreaux.shared.commons.autoconfiguration.proxy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Configuration
@ConditionalOnProperty(prefix = "server.proxy", name = "enabled")
@EnableConfigurationProperties(ProxyProperties.class)
public class ProxyAutoConfiguration {

    public ProxyAutoConfiguration(ProxyProperties properties) {
        if (properties.isEnabled()) {
            System.setProperty("http.proxyHost", properties.getUrl());
            System.setProperty("http.proxyPort", String.valueOf(properties.getPort()));
            if (properties.getUsername() != null) {
                Authenticator.setDefault(new Authenticator() {
                    /**
                     * Called when password authorization is needed.  Subclasses should
                     * override the default implementation, which returns null.
                     *
                     * @return The PasswordAuthentication collected from the
                     * user, or null if none is provided.
                     */
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getUsername(), properties.getPassword() == null ? null : properties.getPassword().toCharArray());
                    }
                });
            }
        }
    }
}
