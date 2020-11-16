package ru.geekbrains.clienthandler;

import lombok.extern.log4j.Log4j2;
import ru.geekbrains.core.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;
    private String password;

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            startWorkerThread();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private void startWorkerThread() {
        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(MessageManager.AUTH.getText())) {
                        // /auth login1 pass1
                        String[] tokens = msg.split(" ", 3);
                        String nickname = server.getAuthHandler().getNickByLoginPass(tokens[1], tokens[2]);
                        if (nickname != null) {
                            if (server.isNickBusy(nickname)) {
                                out.writeUTF("Учетная запись уже используется!");
                                log.info("Учетная запись с логином: {} уже существует!", tokens[1]);
                                continue;
                            }
                            out.writeUTF(MessageManager.AUTH_OK.getText() + nickname);
                            this.nickname = nickname;
                            this.login = tokens[1];
                            this.password = tokens[2];
                            server.subscribe(this);
                            break;
                        } else {
                            out.writeUTF("Неверный логин/пароль!");
                            log.info("Неверный логин или пароль: логин {}, пароль: {}", tokens[1], tokens[2]);
                        }
                    }
                    if (msg.startsWith(MessageManager.REG.getText())) {
                        // /reg login1 pass1 nick1
                        String[] tokens = msg.split(" ", 4);
                        if (tokens[3].isEmpty()) {
                            out.writeUTF("Введите ник в поле ввода сообщений!");
                            log.info("Введите ник в поле ввода сообщений!");
                            continue;
                        }
                        String nickname = server.getAuthHandler().getNickByLoginPass(tokens[1], tokens[2]);
                        if (nickname != null) {
                            out.writeUTF("Пользователь с таким ником уже существует!");
                            log.info("Пользователь с таким ником уже существует! Ник: {}", nickname);
                            continue;
                        }
                        if (server.isLoginBusy(tokens[1])) {
                            out.writeUTF("Учетная запись с таким логином уже существует!");
                            log.info("Учетная запись с таким логином уже существует! Логин: {}, пароль: {}", tokens[1], tokens[2]);
                            continue;
                        }
                        if (server.getAuthHandler().addUser(tokens[1], tokens[2], tokens[3])) {
                            out.writeUTF(MessageManager.REG_OK.getText() + tokens[3]);
                            this.login = tokens[1];
                            this.password = tokens[2];
                            this.nickname = tokens[3];
                            // создаем каталог для нового пользователя
                            createCatalogFromNewUser();
                            server.subscribe(this);
                            break;
                        } else {
                            out.writeUTF("Не удалось зарегистрироваться попробуйте еще раз!");
                            log.info("Не удалось зарегистрироваться попробуйте еще раз!");
                        }
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(MessageManager.SERVICE_SYMBOL.getText())) {
                        if (msg.startsWith(MessageManager.WISP.getText())) {
                            // /w nick1 text message
                            String[] tokens = msg.split(" ", 3);
                            server.sendPrivateMsg(this, tokens[1], tokens[2]);
                            continue;
                        }
                        if (msg.startsWith(MessageManager.END.getText())) {
                            sendMessage(MessageManager.END_CONFIRM.getText());
                            break;
                        }
                        if (msg.startsWith(MessageManager.SERVER_DOWN_OK.getText())) {
                            break;
                        }
                        if (msg.startsWith(MessageManager.CHANGE_NICKNAME.getText())) {
                            // /change_nickname newNickname
                            String[] tokens = msg.split(" ", 2);
                            if (server.getAuthHandler().changeNickName(login, tokens[1])) {
                                this.nickname = tokens[1];
                                sendMessage(MessageManager.CHANGE_NICKNAME_CONFIRM.getText() + nickname);
                                server.broadcastClientsList();
                            }
                            continue;
                        }
                        // получение файла от пользователя
                        if (msg.startsWith(MessageManager.CATCH_FILE.getText())) {
                            addingNewFileFromUser(msg.split(" ", 2)[1]);
                            continue;
                        }
                    } else {
                        server.broadcastMsg(this, msg);
                    }
                    System.out.println(msg);
                }
            } catch (IOException e) {
                log.error(e);
                throw new RuntimeException(e);
            } finally {
                closeConnection();
            }
        }).start();
    }

    private void createCatalogFromNewUser() {
        Path path = Paths.get("users_catalog", this.login);
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private void addingNewFileFromUser(String filePath) {

    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
}
