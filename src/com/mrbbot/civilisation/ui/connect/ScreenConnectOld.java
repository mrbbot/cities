package com.mrbbot.civilisation.ui.connect;

import com.mrbbot.civilisation.ui.Screen;
import com.mrbbot.generic.net.ClientOnly;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

@ClientOnly
public class ScreenConnectOld extends Screen implements EventHandler<KeyEvent> {
  private final ConnectionRequestHandler connectionRequestHandler;
  private VBox vBox;
  private TextField hostField, idField;
  private Button connectButton;
  private ProgressIndicator progressIndicator;
  private Label errorLabel;

  public ScreenConnectOld(ConnectionRequestHandler connectionRequestHandler) {
    this.connectionRequestHandler = connectionRequestHandler;
  }

  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    StackPane root = new StackPane();
    root.setAlignment(Pos.CENTER);

    vBox = new VBox(5);
    vBox.setMaxSize(200, 0);

    Label hostLabel = new Label("Host:");
    hostField = new TextField("127.0.0.1");

    Label idLabel = new Label("ID:");
    idField = new TextField("MrBBot");

    hostField.setOnKeyTyped(this);
    idField.setOnKeyTyped(this);

    connectButton = new Button("Connect");
    connectButton.setPrefWidth(200);
    //TODO: change to true when no ID
    connectButton.setDisable(false);

    vBox.getChildren().addAll(hostLabel, hostField, idLabel, idField, connectButton);

    progressIndicator = new ProgressIndicator();
    progressIndicator.setMaxSize(100, 100);
    progressIndicator.setVisible(false);

    root.getChildren().addAll(vBox, progressIndicator);

    connectButton.setOnAction((event) -> {
      setLoading(true);
      Thread thread = new Thread(() -> {
        try {
          connectionRequestHandler.connect(hostField.getText(), idField.getText());
        } catch (IOException e) {
          Platform.runLater(() -> showError("Error: " + e.getMessage()));
          e.printStackTrace();
        }
      });
      thread.setName("ClientLauncher");
      thread.start();
    });

    return new Scene(root, width, height);
  }

  private void setLoading(boolean loading) {
    vBox.setVisible(!loading);
    progressIndicator.setVisible(loading);
  }

  private void showError(String error) {
    if(errorLabel != null) {
      errorLabel.setText(error);
    } else {
      errorLabel = new Label(error);
      errorLabel.setTextFill(Color.RED);
      vBox.getChildren().add(errorLabel);
    }
    setLoading(false);
  }

  @Override
  public void handle(KeyEvent event) {
    connectButton.setDisable(hostField.getText().isEmpty() || idField.getText().isEmpty());
  }
}
