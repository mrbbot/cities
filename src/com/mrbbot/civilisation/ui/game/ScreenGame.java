package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.map.Map;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.civilisation.render.RenderGame;
import com.mrbbot.civilisation.render.map.RenderMap;
import com.mrbbot.civilisation.ui.Screen;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ScreenGame extends Screen {
  private final Map map;
  private final String id;
  public RenderGame renderGame;
  private UIGame ui;

  public ScreenGame(Map map, String id) {
    this.map = map;
    this.id = id;
  }

  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    StackPane pane = new StackPane();
    pane.setAlignment(Pos.TOP_LEFT);

    RenderMap renderMap = new RenderMap(map, id, this::onSelectedUnitChanged);
    renderGame = new RenderGame(renderMap, width, height);

    ui = new UIGame(renderMap, width, height);
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

  private void onSelectedUnitChanged(Unit unit) {
    ui.onSelectedUnitChanged(unit);
  }

  public void handlePacketChat(PacketChat packet) {
    ui.handlePacketChat(packet);
  }
}
