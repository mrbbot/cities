package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.render.RenderGame;
import com.mrbbot.civilisation.render.map.RenderMap;
import com.mrbbot.civilisation.ui.UIGame;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.BitSet;

public class Civilisation
  extends Application {

  @Override
  public void start(Stage primaryStage) {
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    int width = (int) screenBounds.getWidth();
    int height = (int) screenBounds.getHeight();

    StackPane pane = new StackPane();
    pane.setAlignment(Pos.TOP_LEFT);

    Map map = new Map();
    RenderMap renderMap = new RenderMap(map);
    RenderGame renderGame = new RenderGame(renderMap, width, height);

    UIGame ui = new UIGame(renderMap, width, height);
    ui.setPrefSize(width, height);

    pane.getChildren().addAll(renderGame.subScene, ui);
    Scene scene = new Scene(pane, width, height);
    renderGame.setScene(scene, e -> {
      if(e.getCode() == KeyCode.F11) {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
      }
    });
    primaryStage.setTitle("Civilisation");
    primaryStage.setResizable(false);
    primaryStage.setScene(scene);
    primaryStage.setFullScreen(true);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

}

