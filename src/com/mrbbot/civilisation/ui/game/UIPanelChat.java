package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.generic.net.ClientOnly;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

@ClientOnly
class UIPanelChat extends BorderPane {
  private TextArea log;
  private TextField messageField;
  private Button sendButton;

  UIPanelChat(String id) {
    super();
    setPrefSize(250, 150);

    log = new TextArea("");
    log.setEditable(false);
    log.textProperty().addListener((observable, oldValue, newValue) -> log.setScrollTop(Double.MAX_VALUE));

    BorderPane bottomPane = new BorderPane();
    bottomPane.setPadding(new Insets(5, 0, 0, 0));

    messageField = new TextField();
    bottomPane.setCenter(messageField);
    messageField.textProperty().addListener((observable, oldValue, newValue) -> sendButton.setDisable(newValue.isEmpty()));

    sendButton = new Button("Send");
    sendButton.setOnAction(e -> {
      String message = id + "> " + messageField.getText();
      addMessage(message);
      Civilisation.CLIENT.broadcast(new PacketChat(message));
      messageField.setText("");
    });
    sendButton.setDisable(true);
    StackPane sendButtonPane = new StackPane(sendButton);
    sendButtonPane.setPadding(new Insets(0, 0, 0, 5));
    bottomPane.setRight(sendButtonPane);

    setCenter(log);
    setBottom(bottomPane);
  }

  void addMessage(String message) {
    String currentText = log.getText();
    String toAdd = message;
    if(!currentText.equals("")) toAdd = "\n" + toAdd;
    log.appendText(toAdd);
  }
}
