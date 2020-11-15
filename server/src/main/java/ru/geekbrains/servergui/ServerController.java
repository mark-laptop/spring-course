package ru.geekbrains.servergui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.geekbrains.clienthandler.ClientHandler;
import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.config.Config;
import ru.geekbrains.core.Server;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerController implements Initializable {

    private Server server;
    private AnnotationConfigApplicationContext context;

    @FXML
    private TextField portField;
    @FXML
    private Label textInfoStatusServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textInfoStatusServer.setText("Сервере остановлен!");
        this.portField.setText("8189");
         this.context = new AnnotationConfigApplicationContext(Config.class);
    }

    public void startServer(ActionEvent actionEvent) {
        if (server != null) return;
        String port = portField.getText();
        if (port.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Необходимо ввести номер порта", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        if (!isNumber(port)) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Номер порта состоит только из цифр", ButtonType.OK);
                alert.showAndWait();
            });
            return;
        }
        this.server = this.context.getBean(Server.class);
        this.server.setPort(Integer.parseInt(portField.getText()));
        new Thread(() -> server.start()).start();
        textInfoStatusServer.setText("Сервер запущен!");
    }

    public void stopServer(ActionEvent actionEvent) {
        if (this.server == null) return;
        List<ClientHandler> clients = this.server.getClients();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(MessageManager.SERVER_DOWN.getText());
        }
        clients.clear();
        if (this.server != null) {
            this.context.stop();
            this.server.stop();
            this.server = null;
        }
        textInfoStatusServer.setText("Сервере остановлен!");
    }

    private boolean isNumber(String string) {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }
}
