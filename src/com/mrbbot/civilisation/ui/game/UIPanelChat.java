package com.mrbbot.civilisation.ui.game;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

class UIPanelChat extends BorderPane {
  UIPanelChat() {
    super();
    setPrefSize(250, 150);

    TextArea log = new TextArea("Log thing");
    log.setEditable(false);

    BorderPane bottomPane = new BorderPane();
    bottomPane.setPadding(new Insets(5, 0, 0, 0));

    TextField messageField = new TextField();
    bottomPane.setCenter(messageField);

    Button sendButton = new Button("Send");
    StackPane sendButtonPane = new StackPane(sendButton);
    sendButtonPane.setPadding(new Insets(0, 0, 0, 5));
    bottomPane.setRight(sendButtonPane);

    setCenter(log);
    setBottom(bottomPane);
  }
}
