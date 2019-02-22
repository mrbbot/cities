package com.mrbbot.civilisation.ui.game;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.PlayerStats;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Improvement;
import com.mrbbot.civilisation.logic.techs.PlayerTechDetails;
import com.mrbbot.civilisation.logic.techs.Tech;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.logic.unit.UnitType;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.civilisation.render.map.RenderGame;
import com.mrbbot.generic.net.ClientOnly;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;

@ClientOnly
public class UIGame extends AnchorPane {
  private static final Insets PANEL_PADDING = new Insets(10);

  private final RenderGame renderGame;
  private final Color playerColor;

  private final UIPanelTech panelTech;
  private final UIPanelChat panelChat;
  private final UIPanelActions panelActions;
  private final UIPanelStats panelStats;
  private final UITechTree techTree;
  private final UIPanelCityDetails panelCityDetails;
  private final Button closeTechTreeButton;

  public UIGame(RenderGame renderGame, int width, int height) {
    this.renderGame = renderGame;
    setPickOnBounds(false);

    Player player = this.renderGame.currentPlayer;
    playerColor = player.getColour();

    panelTech = new UIPanelTech();
    panelTech.setBorder(makePanelBorder(Pos.BOTTOM_RIGHT));
    panelTech.setBackground(makePanelBackground(Pos.BOTTOM_RIGHT));
    panelTech.setPadding(PANEL_PADDING);
    panelTech.setOnOpenTechTree(e -> setTechTreeVisible(true));
    AnchorPane.setTopAnchor(panelTech, 0.0);
    AnchorPane.setLeftAnchor(panelTech, 0.0);

    panelChat = new UIPanelChat(renderGame.currentPlayer.id);
    panelChat.setBorder(makePanelBorder(Pos.BOTTOM_LEFT));
    panelChat.setBackground(makePanelBackground(Pos.BOTTOM_LEFT));
    panelChat.setPadding(PANEL_PADDING);
    AnchorPane.setTopAnchor(panelChat, 0.0);
    AnchorPane.setRightAnchor(panelChat, 0.0);

    panelActions = new UIPanelActions();
    panelActions.setBorder(makePanelBorder(Pos.TOP_LEFT));
    panelActions.setBackground(makePanelBackground(Pos.TOP_LEFT));
    panelActions.setPadding(PANEL_PADDING);
    panelActions.setUnitActionListener(this::onUnitAction);
    AnchorPane.setBottomAnchor(panelActions, 0.0);
    AnchorPane.setRightAnchor(panelActions, 0.0);

    panelStats = new UIPanelStats();
    panelStats.setBorder(makePanelBorder(Pos.TOP_RIGHT));
    panelStats.setBackground(makePanelBackground(Pos.TOP_RIGHT));
    panelStats.setPadding(PANEL_PADDING);
    AnchorPane.setBottomAnchor(panelStats, 0.0);
    AnchorPane.setLeftAnchor(panelStats, 0.0);

    panelCityDetails = new UIPanelCityDetails(renderGame);
    panelCityDetails.setBorder(new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      CornerRadii.EMPTY,
      new BorderWidths(0, 0, 0, 10)
    )));
    panelCityDetails.setBackground(makePanelBackground(Pos.CENTER));
    panelCityDetails.setVisible(false);
    AnchorPane.setTopAnchor(panelCityDetails, 0.0);
    AnchorPane.setRightAnchor(panelCityDetails, 0.0);
    AnchorPane.setBottomAnchor(panelCityDetails, 0.0);

    getChildren().addAll(panelTech, panelChat, panelActions, panelStats, panelCityDetails);

    PlayerTechDetails techDetails = new PlayerTechDetails(
      renderGame.data.getPlayerUnlockedTechs(player.id),
      renderGame.data.getPlayerUnlockingTech(player.id),
      renderGame.data.getPlayerUnlockingProgress(player.id)
    );
    techTree = new UITechTree(
      renderGame.data,
      player.id,
      techDetails,
      height
    );
    techTree.setBorder(makePanelBorder(Pos.CENTER));
    AnchorPane.setTopAnchor(techTree, 0.0);
    AnchorPane.setLeftAnchor(techTree, 0.0);
    AnchorPane.setBottomAnchor(techTree, 0.0);
    AnchorPane.setRightAnchor(techTree, 0.0);
    panelTech.setTechDetails(techDetails);

    closeTechTreeButton = new Button("Close Tech Tree");
    closeTechTreeButton.setOnAction(e -> setTechTreeVisible(false));
    AnchorPane.setTopAnchor(closeTechTreeButton, 20.0);
    AnchorPane.setLeftAnchor(closeTechTreeButton, 20.0);
  }

  private CornerRadii makeCornerRadiiForCutout(Pos cutout, int size) {
    return new CornerRadii(
      cutout == Pos.TOP_LEFT ? size : 0,
      cutout == Pos.TOP_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_RIGHT ? size : 0,
      cutout == Pos.BOTTOM_LEFT ? size : 0,
      false
    );
  }

  private Border makePanelBorder(Pos cutout) {
    return new Border(new BorderStroke(
      playerColor,
      BorderStrokeStyle.SOLID,
      makeCornerRadiiForCutout(cutout, 10),
      new BorderWidths(10)
    ));
  }

  private Background makePanelBackground(Pos cutout) {
    return new Background(new BackgroundFill(
      Color.WHITE,
      makeCornerRadiiForCutout(cutout, 20),
      null
    ));
  }

  void onSelectedUnitChanged(Game game, Unit unit) {
    panelActions.setSelectedUnit(game, unit);
  }

  void onSelectedCityChanged(Game game, City city, ArrayList<City> playersCities) {
    if (city != null) panelCityDetails.setSelectedCity(game, city, playersCities);
    panelCityDetails.setVisible(city != null);
  }

  private void onUnitAction(Unit unit, String actionDetails) {
    if (unit == null) {
      System.out.println("Next turn...");
      renderGame.data.waitingForPlayers = true;
      renderGame.setSelectedUnit(null);
      renderGame.setSelectedCity(null);
    } else {
      System.out.println((unit.unitType.getName() + " performed an action (details: \"" + actionDetails + "\")"));
      if (unit.hasAbility(UnitAbility.ABILITY_SETTLE)) {
        Civilisation.CLIENT.broadcast(new PacketCityCreate(renderGame.currentPlayer.id, unit.tile.x, unit.tile.y));
        renderGame.data.cities.add(new City(renderGame.data.hexagonGrid, unit.tile.x, unit.tile.y, renderGame.currentPlayer));
        renderGame.updateTileRenders();
        renderGame.setSelectedUnit(null);
        renderGame.deleteUnit(unit, true);
      } else if(unit.hasAbility(UnitAbility.ABILITY_IMPROVE)) {
        Improvement improvement = Improvement.fromName(actionDetails);
        assert improvement != null;
        PacketWorkerImproveRequest packetWorkerImproveRequest = new PacketWorkerImproveRequest(unit.tile.x, unit.tile.y, improvement);
        renderGame.data.handlePacket(packetWorkerImproveRequest);
        Civilisation.CLIENT.broadcast(packetWorkerImproveRequest);
        renderGame.setSelectedUnit(null);
      } else if(unit.unitType.getUpgrade() != null) {
        PacketUnitUpgrade packetUnitUpgrade = new PacketUnitUpgrade(unit.tile.x, unit.tile.y);
        renderGame.data.handlePacket(packetUnitUpgrade);
        Civilisation.CLIENT.broadcast(packetUnitUpgrade);
        renderGame.setSelectedUnit(null);
      } else if(unit.hasAbility(UnitAbility.ABILITY_BLAST_OFF)) {
        //TODO: win game
        System.out.println("Win game here");
      }
    }
  }

  private void setTechTreeVisible(boolean visible) {
    if (visible) {
      getChildren().add(techTree);
      getChildren().add(closeTechTreeButton);
    } else {
      getChildren().remove(techTree);
      getChildren().remove(closeTechTreeButton);
    }
  }

  void handlePacketChat(PacketChat packet) {
    panelChat.addMessage(packet.message);
  }

  void handlePacketReady(PacketReady data) {
    panelActions.setNextTurnWaiting(data.ready);
  }

  void onPlayerStatsChanged(PlayerStats stats) {
    panelStats.setPlayerStats(stats);
  }

  void onTechDetailsChanged(Game game, PlayerTechDetails details) {
    panelTech.setTechDetails(details);
    techTree.setTechDetails(game, details);
  }
}
