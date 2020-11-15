package ru.geekbrains.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteDataSource;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class FactoryConnectionDB {

    private Connection connection;
    private SQLiteDataSource dataSource;
    private static FactoryConnectionDB instance;

    @Autowired
    public void setDataSource(SQLiteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = this.dataSource.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized Connection getConnection() {
        try {
            if (connection != null && connection.isClosed()) {
                connection = this.dataSource.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized static FactoryConnectionDB getFactoryConnectionDB() {
        if (instance != null) {
            return instance;
        }
        instance = new FactoryConnectionDB();
        return instance;
    }

    void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
