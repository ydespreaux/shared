package com.ydespreaux.shared.commons.autoconfiguration.proxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProxyAutoConfiguration.class)
@ActiveProfiles(profiles = {"noproxy"})
public class NoProxyAutoConfigurationTest {

    @Test
    public void loadContext(){
        assertThat(System.getProperty("http.proxyHost"), is(nullValue()));
    }
}
