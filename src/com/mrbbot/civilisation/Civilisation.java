package com.mrbbot.civilisation;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.render.RenderGame;
import com.mrbbot.civilisation.render.map.RenderMap;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Civilisation
  extends Application {

  @Override
  public void start(Stage primaryStage) {
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    int width = (int) screenBounds.getWidth();
    int height = (int) screenBounds.getHeight();

    BorderPane borderPane = new BorderPane();

    Map map = new Map();
    RenderMap renderMap = new RenderMap(map);
    RenderGame renderGame = new RenderGame(renderMap, width, height);

    borderPane.setCenter(renderGame.subScene);
    Scene scene = new Scene(borderPane, width, height);
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

