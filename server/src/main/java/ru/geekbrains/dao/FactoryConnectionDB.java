package ru.geekbrains.dao;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FactoryConnectionDB {

    private String urlDatabase;
    private Connection connection;
    private static FactoryConnectionDB instance;

    public void setUrlDatabase(String urlDatabase) {
        this.urlDatabase = urlDatabase;
    }

    public void init() {
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(urlDatabase);
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized Connection getConnection() {
        try {
            if (connection != null && connection.isClosed()) {
                connection = DriverManager.getConnection(urlDatabase);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public synchronized static FactoryConnectionDB getFactoryConnectionDB() throws SQLException, ClassNotFoundException {
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
