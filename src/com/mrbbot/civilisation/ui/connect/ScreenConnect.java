package com.mrbbot.civilisation.ui.connect;

import com.mrbbot.civilisation.ui.Screen;
import com.mrbbot.generic.net.ClientOnly;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

@ClientOnly
public class ScreenConnect extends Screen {
  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    VBox root = new VBox(20);
    root.setAlignment(Pos.CENTER);

    root.getChildren().add(makeChoicePane());
    root.getChildren().add(makeJoinPane());

    return new Scene(root, width, height);
  }

  private TitledPane makeTitledPane(String title, Node child) {
    TitledPane titledPane = new TitledPane(title, child);
    titledPane.setMaxSize(300, 0);
    titledPane.setPrefWidth(300);
    titledPane.setCollapsible(false);
    return titledPane;
  }

  private TitledPane makeChoicePane() {
    Label headerLabel = new Label("I want to...");
    Label footerLabel = new Label("...a game");

    ToggleGroup toggleGroup = new ToggleGroup();
    RadioButton joinButton = new RadioButton("Join");
    joinButton.setToggleGroup(toggleGroup);
    joinButton.setSelected(true);
    RadioButton hostButton = new RadioButton("Host");
    hostButton.setToggleGroup(toggleGroup);
    HBox buttonBox = new HBox(10, joinButton, hostButton);
    buttonBox.setAlignment(Pos.CENTER);

    VBox pane = new VBox(10, headerLabel, buttonBox, footerLabel);
    pane.setAlignment(Pos.CENTER);

    return makeTitledPane("Game", pane);
  }

  private TitledPane makeJoinPane() {
    GridPane pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setMaxWidth(300);

    Label hostLabel = new Label("Host");
    Label portLabel = new Label("Port");
    Label idLabel = new Label("ID");

    TextField hostField = new TextField();
    TextField portField = new TextField();
    TextField idField = new TextField();

    pane.add(hostLabel, 0, 0);
    pane.add(hostField, 1, 0, 2, 1);
    pane.add(portLabel, 3, 0);
    pane.add(portField, 4, 0, 1, 1);
    pane.add(idLabel, 0, 1);
    pane.add(idField, 1, 1, 4, 1);

    pane.add(new Rectangle(40, 10, Color.RED), 0, 2);
    pane.add(new Rectangle(40, 10, Color.ORANGERED), 1, 2);
    pane.add(new Rectangle(40, 10, Color.YELLOW), 2, 2);
    pane.add(new Rectangle(40, 10, Color.GREEN), 3, 2);
    pane.add(new Rectangle(40, 10, Color.BLUE), 4, 2);

    return makeTitledPane("Join", pane);
  }
}
