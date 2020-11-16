package ru.geekbrains.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class FactoryConnectionDB {

    private Connection connection;
    private final DataSource dataSource;

    @Autowired
    public FactoryConnectionDB(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try {
            connection = this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = this.dataSource.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
