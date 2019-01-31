package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.render.RenderGame;
import com.mrbbot.civilisation.render.map.RenderMap;
import com.mrbbot.civilisation.ui.Screen;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ScreenGame extends Screen {
  private final Map map;
  public RenderGame renderGame;

  public ScreenGame(Map map) {
    this.map = map;
  }

  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    StackPane pane = new StackPane();
    pane.setAlignment(Pos.TOP_LEFT);

    RenderMap renderMap = new RenderMap(map);
    renderGame = new RenderGame(renderMap, width, height);

    UIGame ui = new UIGame(renderMap, width, height);
    ui.setPrefSize(width, height);

    pane.getChildren().addAll(renderGame.subScene, ui);
    Scene scene = new Scene(pane, width, height);
    renderGame.setScene(scene, e -> {
      if(e.getCode() == KeyCode.F11) {
        stage.setFullScreen(!stage.isFullScreen());
      }
    });
    return scene;
  }
}
