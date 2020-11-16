package ru.geekbrains.dao;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Log4j2
public class DBAuthHandler implements AuthHandler {

    private static final String ADD_NEW_USER = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
    private static final String CHANGE_NICKNAME_USER = "UPDATE users SET nickname = ? WHERE login = ?;";
    private static final String GET_LOGIN_USER = "SELECT login FROM users WHERE login = ?;";
    private static final String GET_NICKNAME_USER = "SELECT nickname FROM users WHERE login = ? AND password = ?;";

    private final DataSource dataSource;

    @Autowired
    public DBAuthHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void start() {
        log.debug("AuthHandler started...");
    }

    @Override
    public void stop() {
        log.debug("AuthHandler stopped...");
    }

    @Override
    public synchronized boolean addUser(String login, String password, String nickname) {
        int result = 0;
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(ADD_NEW_USER)) {
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, nickname);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return result != 0;
    }

    @Override
    public synchronized boolean changeNickName(String login, String newNickname) {
        int result = 0;
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHANGE_NICKNAME_USER)) {
            statement.setString(1, newNickname);
            statement.setString(2, login);
            result = statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return result != 0;
    }

    @Override
    public synchronized boolean isLoginBusy(String login) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_LOGIN_USER)) {
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return true;
        } catch (SQLException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public synchronized String getNickByLoginPass(String login, String password) {
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_NICKNAME_USER)) {
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        } catch (SQLException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        return null;
    }
}
