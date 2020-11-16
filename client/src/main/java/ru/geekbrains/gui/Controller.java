package ru.geekbrains.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.geekbrains.clienthandler.MessageManager;
import ru.geekbrains.network.Network;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {
    private boolean authorized;
    private Network network;
    private String nickname;
    private ObservableList<String> clientsList;

    @FXML
    TextField msgField, loginField, addressField, portField;

    @FXML
    TextArea mainTextArea;

    @FXML
    PasswordField passField;

    @FXML
    HBox authPanel, msgPanel, connectPanel;

    @FXML
    ListView<String> clientsView;


    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (this.authorized) {
            this.authPanel.setVisible(false);
            this.authPanel.setManaged(false);
            this.connectPanel.setVisible(false);
            this.connectPanel.setManaged(false);
            /*Доработать регистрацию через всплывающее окно ввода данных*/
//            msgPanel.setVisible(true);
//            msgPanel.setManaged(true);
            this.clientsView.setVisible(true);
            this.clientsView.setManaged(true);
        } else {
            this.authPanel.setVisible(true);
            this.authPanel.setManaged(true);
            this.connectPanel.setVisible(true);
            this.connectPanel.setManaged(true);
            /*Доработать регистрацию через всплывающее окно ввода данных*/
//            msgPanel.setVisible(false);
//            msgPanel.setManaged(false);
            this.clientsView.setVisible(false);
            this.clientsView.setManaged(false);
            this.nickname = "";
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        this.clientsList = FXCollections.observableArrayList();
        this.clientsView.setItems(this.clientsList);
        this.network = new Network();
    }

    public void sendMsg() {
        if (this.network.isNotConnected()) {
            showAlert("Авторизируйтесь для отправки сообщений");
            this.msgField.clear();
            this.msgField.requestFocus();
            return;
        }
        this.network.sendMessage(this.msgField.getText());
        this.msgField.clear();
        this.msgField.requestFocus();
    }

    public void sendAuth(ActionEvent actionEvent) {
        if(!connectDataIsCorrect()) return;
        this.network.setAddress(this.addressField.getText());
        this.network.setPort(this.portField.getText());
        if (this.network.isNotConnected()) {
            try {
                connected();
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
        if (!this.network.isNotConnected()) {
            if (this.network.sendMessage(MessageManager.AUTH.getText() + this.loginField.getText() + " " + this.passField.getText())) {
                this.loginField.clear();
                this.passField.clear();
            } else {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
    }

    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void clickClientsList(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String str = this.clientsView.getSelectionModel().getSelectedItem();
            this.msgField.setText(MessageManager.WISP.getText() + str + " ");
            this.msgField.requestFocus();
            this.msgField.selectEnd();
        }
    }

    public void sendReg(ActionEvent actionEvent) {
        if(!connectDataIsCorrect()) return;
        this.network.setAddress(this.addressField.getText());
        this.network.setPort(this.portField.getText());
        if (this.network.isNotConnected()) {
            try {
                connected();
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
        if (!this.network.isNotConnected()) {
            if (this.network.sendMessage(MessageManager.REG.getText() + this.loginField.getText() + " " + this.passField.getText() + " " + this.msgField.getText())) {
                this.loginField.clear();
                this.passField.clear();
                this.msgField.clear();
            } else {
                showAlert("Невозможно подключиться к серверу, проверьте сетевое соединение...");
            }
        }
    }

    private boolean connectDataIsCorrect() {
        if (this.addressField.getText().isEmpty()) {
            showAlert("Не заполен адресс сервера!");
            return false;
        }
        if (!isNumber(this.portField.getText())) {
            showAlert("Порт должен содержать только цифры!");
            return false;
        }
        return true;
    }

    private boolean isNumber(String string) {
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(string);
        return matcher.matches();
    }

    private void connected() throws IOException {
        this.network.connect(
                argsGetMessage -> this.mainTextArea.appendText(argsGetMessage[0]),
                argsAuthOk -> {
                    this.nickname = argsAuthOk[0];
                    setAuthorized(true);
                },
                argsGetClientsList -> Platform.runLater(() -> {
                    this.clientsList.clear();
                    for (int i = 1; i < argsGetClientsList.length; i++) {
                        if (argsGetClientsList[i].equals(this.nickname)) continue;
                        this.clientsList.add(argsGetClientsList[i]);
                    }
                }),
                argsDisconnect -> {
                    showAlert("Произошло отключение от сервера");
                    setAuthorized(false);
                }
        );
    }
}
