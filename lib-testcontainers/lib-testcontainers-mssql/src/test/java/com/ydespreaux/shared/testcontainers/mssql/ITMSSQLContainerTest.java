package com.ydespreaux.shared.testcontainers.mssql;

import com.ydespreaux.shared.testcontainers.mssql.MSSQLServerContainer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
public class ITMSSQLContainerTest {

    @ClassRule
    public static MSSQLServerContainer msSqlContainer = new MSSQLServerContainer()
            //.withDatabaseName("an_database")
            .withUsername("SA")
            .withPassword("A_Str0ng_Required_Password")
            //.withRootPassword("rootpwd")
            //.withConfigurationOverride("mysql-test-conf")
            .withDriverClassSystemProperty("jdbc.SQLServerDriver")
            .withUrlSystemProperty("jdbc.url")
            .withUsernameSystemProperty("jdbc.username")
            .withPasswordSystemProperty("jdbc.password")
            .withPlatformSystemProperty("jdbc.platform")
            ;

    /*
    private HikariConfig hikariConfig;
    private HikariDataSourcePoolMetadata ds;

    @Before
    public void setup() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mssqlServer.getJdbcUrl());
        hikariConfig.setUsername(mssqlServer.getUsername());
        hikariConfig.setPassword(mssqlServer.getPassword());

        HikariDataSource ds = new HikariDataSource(hikariConfig);
    }
    */

    @Test
    public void environmentSystemProperty() {
        assertThat(System.getProperty(msSqlContainer.getDriverClassSystemProperty()), is(equalTo(msSqlContainer.getDriverClassName())));
        assertThat(System.getProperty(msSqlContainer.getUsernameSystemProperty()), is(equalTo("SA")));
        assertThat(System.getProperty(msSqlContainer.getPasswordSystemProperty()), is(equalTo("A_Str0ng_Required_Password")));
        assertThat(System.getProperty(msSqlContainer.getPlatformSystemProperty()), is(equalTo("sqlserver")));
        assertThat(System.getProperty(msSqlContainer.getUrlSystemProperty()), is(equalTo("jdbc:sqlserver://" + msSqlContainer.getContainerIpAddress() + ":" + msSqlContainer.getPort() + ";databaseName=" + msSqlContainer.getDatabaseName())));
//        assertThat(System.getProperty(msSqlContainer.getUrlSystemProperty()), is(equalTo("jdbc:sqlserver://" + msSqlContainer.getContainerIpAddress() + ":" + msSqlContainer.getPort())));
    }

    @Test
    public void getJdbcUrl(){
        String url = format("jdbc:sqlserver://%s:%d", msSqlContainer.getContainerIpAddress(), msSqlContainer.getPort());
        assertThat(msSqlContainer.getJdbcUrl(), is(equalTo(url)));
    }

    @Test
    public void getURL(){
        String url = format("jdbc:sqlserver://%s:%d;databaseName=%s", msSqlContainer.getContainerIpAddress(), msSqlContainer.getPort(), msSqlContainer.getDatabaseName());
        assertThat(msSqlContainer.getURL(), is(equalTo(url)));
    }

    @Test
    public void getInternalURL(){
        String url = format("jdbc:sqlserver://%s:%d;databaseName=%s", msSqlContainer.getNetworkAliases().get(0), 1433, msSqlContainer.getDatabaseName());
        assertThat(msSqlContainer.getInternalURL(), is(equalTo(url)));
    }

}
