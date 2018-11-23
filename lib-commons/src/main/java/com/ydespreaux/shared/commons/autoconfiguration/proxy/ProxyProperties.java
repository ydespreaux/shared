package com.ydespreaux.shared.commons.autoconfiguration.proxy;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "server.proxy")
public class ProxyProperties {

    private boolean enabled;
    private String url;
    private Integer port;
    private String username;
    private String password;
}
