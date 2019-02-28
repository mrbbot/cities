package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * UI panel for sending/receiving chat messages.
 */
@ClientOnly
class UIPanelChat extends BorderPane {
  /**
   * Log of previous chat messages
   */
  private TextArea log;
  /**
   * Text field containing text to send in next message
   */
  private TextField messageField;
  /**
   * Button that when clicked will send the message
   */
  private Button sendButton;

  /**
   * Creates a new chat panel
   *
   * @param id id of the current player
   */
  UIPanelChat(String id) {
    super();
    setPrefSize(250, 150);

    // Create the chat log
    log = new TextArea("");
    log.setEditable(false);
    // Keep the scroll bar at the bottom of the log when new messages arrive
    log.textProperty().addListener(
      (observable, oldValue, newValue) -> log.setScrollTop(Double.MAX_VALUE)
    );

    BorderPane bottomPane = new BorderPane();
    bottomPane.setPadding(new Insets(5, 0, 0, 0));

    // Create the message field
    messageField = new TextField();
    bottomPane.setCenter(messageField);
    // Watch the message and enable the send button when any text has been
    // typed in
    messageField.textProperty().addListener(
      (observable, oldValue, newValue) -> sendButton.setDisable(
        newValue.isEmpty()
      )
    );

    // Create the send button
    sendButton = new Button("Send");
    sendButton.setOnAction(e -> {
      // Build the message
      String message = id + "> " + messageField.getText();
      // Add the message locally, and send it to other clients
      addMessage(message);
      Civilisation.CLIENT.broadcast(new PacketChat(message));
      // Clear the message text
      messageField.setText("");
    });
    sendButton.setDisable(true);
    StackPane sendButtonPane = new StackPane(sendButton);
    sendButtonPane.setPadding(new Insets(0, 0, 0, 5));
    bottomPane.setRight(sendButtonPane);

    setCenter(log);
    setBottom(bottomPane);
  }

  /**
   * Adds a chat message to this panel's chat log
   *
   * @param message new message to add with the player id of the sender
   */
  void addMessage(String message) {
    String currentText = log.getText();
    String toAdd = message;
    // Add a new line to the message if there's already text there
    if (!currentText.equals("")) toAdd = "\n" + toAdd;
    // Append the text to the chat log
    log.appendText(toAdd);
  }
}
