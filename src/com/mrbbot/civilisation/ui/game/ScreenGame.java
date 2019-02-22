package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.net.packet.PacketChat;
import com.mrbbot.civilisation.net.packet.PacketReady;
import com.mrbbot.civilisation.render.RenderCivilisation;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.civilisation.ui.Screen;
import com.mrbbot.civilisation.ui.UIHelpers;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ClientOnly
public class ScreenGame extends Screen {
  private final Game game;
  private final String id;
  public RenderCivilisation renderCivilisation;
  private UIGame ui;

  public ScreenGame(Game game, String id) {
    this.game = game;
    this.id = id;
  }

  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    StackPane pane = new StackPane();
    pane.setAlignment(Pos.TOP_LEFT);

    RenderGame renderGame = new RenderGame(
      game,
      id,
      (unit) -> ui.onSelectedUnitChanged(game, unit),
      (city) -> ui.onSelectedCityChanged(game, city, game.getPlayersCitiesById(id))
    );
    this.renderCivilisation = new RenderCivilisation(renderGame, width, height);

    ui = new UIGame(renderGame, width, height);
    ui.setPrefSize(width, height);
    game.setCurrentPlayer(id, (stats) -> ui.onPlayerStatsChanged(stats));
    game.setTechDetailsListener((details) -> ui.onTechDetailsChanged(game, details));
    game.setMessageListener(UIHelpers::showDialog);

    pane.getChildren().addAll(this.renderCivilisation.subScene, ui);
    Scene scene = new Scene(pane, width, height);
    scene.getStylesheets().add("/com/mrbbot/civilisation/ui/game/styles.css");
    this.renderCivilisation.setScene(scene, e -> {
      if (e.getCode() == KeyCode.F11) {
        stage.setFullScreen(!stage.isFullScreen());
      }
    });
    return scene;
  }

  public void handlePacketChat(PacketChat packet) {
    ui.handlePacketChat(packet);
  }

  public void handlePacketReady(PacketReady data) {
    ui.handlePacketReady(data);
  }
}
