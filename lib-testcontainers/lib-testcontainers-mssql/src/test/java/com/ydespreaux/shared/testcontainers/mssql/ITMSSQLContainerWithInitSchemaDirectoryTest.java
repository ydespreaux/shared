package com.ydespreaux.shared.testcontainers.mssql;

import com.ydespreaux.shared.testcontainers.mssql.MSSQLServerContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
public class ITMSSQLContainerWithInitSchemaDirectoryTest {

    @ClassRule
    public static MSSQLServerContainer msSqlContainer = new MSSQLServerContainer()
            .withDatabaseName("an_springboot_aa")
            .withMsSqlInitDirectory("mssql-directory")
            ;

    private Connection connection;
    private Statement statement;

    @Before
    public void onSetup() throws SQLException {
        connection = createConnection();
    }

    @After
    public void onTeardown() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void checkDbSchema() throws SQLException {
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM tb_user");
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getInt("id"), is(equalTo(1)));
        assertThat(resultSet.getString("idRh"), is(equalTo("XPAX624")));
        assertThat(resultSet.getString("first_name"), is(equalTo("Jean")));
        assertThat(resultSet.getString("last_name"), is(equalTo("Dupond")));
        assertThat(resultSet.getDate("last_modified"), is(notNullValue()));
    }

    @Test
    public void checkWorkstationDbSchema() throws SQLException {
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM tb_workstation");
        assertThat(resultSet.next(), is(true));
        assertThat(resultSet.getInt("id"), is(equalTo(1)));
        assertThat(resultSet.getString("name"), is(equalTo("WS10002")));
        assertThat(resultSet.getString("serial_number"), is(equalTo("WS-1234-5678")));
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection createConnection() throws SQLException {
        return msSqlContainer.createConnection(";databaseName="+msSqlContainer.getDatabaseName());
    }

}
