package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.geometry.Path;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Game;
import com.mrbbot.civilisation.logic.map.tile.City;
import com.mrbbot.civilisation.logic.map.tile.Tile;
import com.mrbbot.civilisation.logic.unit.Unit;
import com.mrbbot.civilisation.logic.unit.UnitAbility;
import com.mrbbot.civilisation.net.packet.*;
import com.mrbbot.generic.net.ClientOnly;
import com.mrbbot.generic.render.RenderData;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Consumer;

@ClientOnly
public class RenderGame extends RenderData<Game> {
  private final Consumer<Unit> selectedUnitListener;
  private final Consumer<City> selectedCityListener;
  public Player currentPlayer;
  private City selectedCity;

  public RenderGame(
    Game data,
    String id,
    Consumer<Unit> selectedUnitListener,
    Consumer<City> selectedCityListener
  ) {
    super(data);
    this.selectedUnitListener = selectedUnitListener;
    this.selectedCityListener = selectedCityListener;

    for (Player player : data.players) {
      if (player.id.equals(id)) {
        currentPlayer = player;
        break;
      }
    }
    if (currentPlayer == null) {
      throw new IllegalStateException("current player not found in player list");
    }

    final PriorityQueue<RenderTile> tilesToAdd = new PriorityQueue<>((a, b) -> {
      if (a.isTranslucent() && !b.isTranslucent()) return 1;
      else if (b.isTranslucent() && !a.isTranslucent()) return -1;
      else return 0;
    });

    data.hexagonGrid.forEach((tile, hex, x, y) -> {
      RenderTile renderTile = new RenderTile(tile, data.hexagonGrid.getNeighbours(x, y, false));
      tile.renderer = renderTile;

      renderTile.setOnMouseClicked((e) -> {
        if (data.waitingForPlayers) return;

        if (e.getButton() == MouseButton.PRIMARY) {
          if (tile.city != null && tile.city.getCenter().samePositionAs(tile) && tile.city.player.equals(currentPlayer)) {
            setSelectedCity(tile.city);
            setSelectedUnit(null);
            return;
          }
          setSelectedCity(null);
          setSelectedUnit(tile.unit);
        } else if (e.getButton() == MouseButton.SECONDARY) {
          if(selectedCity != null) {
            PacketPurchaseTileRequest packetPurchaseTileRequest = new PacketPurchaseTileRequest(selectedCity.getX(), selectedCity.getY(), tile.x, tile.y);
            if(handlePacket(packetPurchaseTileRequest) != null) {
              Civilisation.CLIENT.broadcast(packetPurchaseTileRequest);
            }
          } else if (data.selectedUnit != null) {
            PacketDamage packetDamage = new PacketDamage(data.selectedUnit.tile.x, data.selectedUnit.tile.y, tile.x, tile.y);
            if (handlePacket(packetDamage) != null) {
              Civilisation.CLIENT.broadcast(packetDamage);
            }
          }
        }
      });

      renderTile.setOnMouseDragged((e) -> {
        if (data.waitingForPlayers) return;

        if (e.getButton() == MouseButton.SECONDARY) {
          if (pathStartTile == null && renderTile.data.unit != null && renderTile.data.unit.player.equals(currentPlayer) && renderTile.data.unit.hasAbility(UnitAbility.ABILITY_MOVEMENT)) {
            pathStartTile = renderTile;
            pathStartTile.setOverlayVisible(true);
          }

          if (pathStartTile != null) {
            RenderTile pickedTile = getTileFromPickResult(e.getPickResult());
            if ((pickedTile == null || pickedTile != pathEndTile) && pathEndTile != null) {
              resetPathfindingEnd();
            }
            if (pickedTile != null) {
              RenderTile potentialEnd = pickedTile;

              List<Tile> path = data.hexagonGrid.findPath(
                pathStartTile.data.x,
                pathStartTile.data.y,
                potentialEnd.data.x,
                potentialEnd.data.y,
                pathStartTile.data.unit.remainingMovementPointsThisTurn
              ).path;
              path.forEach((p) -> p.renderer.setOverlayVisible(true));

              if (path.size() > 1) {
                potentialEnd = path.get(path.size() - 1).renderer;
              }
              pathEndTile = potentialEnd;
              pathEndTile.setOverlayVisible(path.size() > 1);
            }
          }
        }
      });

      tilesToAdd.add(renderTile);
    });

    Point2D topLeftCenter = data.hexagonGrid.get(0, 0).getHexagon().getCenter();
    Box gameBoard = new Box(
      Math.abs(topLeftCenter.getX() * 2) + 4.5,
      Math.abs(topLeftCenter.getY() * 2) + 3,
      1
    );
    gameBoard.setTranslateZ(-0.5);
    gameBoard.setMaterial(new PhongMaterial(Color.WHITESMOKE));
    add(gameBoard);

    RenderTile t;
    while ((t = tilesToAdd.poll()) != null) add(t);
  }

  public void setSelectedUnit(Unit unit) {
    if (unit != null && unit.player.equals(currentPlayer)) {
      if (data.selectedUnit != null) {
        data.selectedUnit.tile.selected = false;
        data.selectedUnit.tile.renderer.updateRender();
      }
      unit.tile.selected = true;
      unit.tile.renderer.updateRender();
      data.selectedUnit = unit.tile.unit;
      selectedUnitListener.accept(data.selectedUnit);
    } else {
      if (data.selectedUnit != null) {
        data.selectedUnit.tile.selected = false;
        data.selectedUnit.tile.renderer.updateRender();
        data.selectedUnit = null;
        selectedUnitListener.accept(null);
      }
    }
  }

  public void setSelectedCity(City city) {
    selectedCity = city;
    selectedCityListener.accept(city);
  }

  public void deleteUnit(Unit unit, boolean broadcast) {
    if (unit != null) {
      if (data.selectedUnit == unit) setSelectedUnit(null);
      unit.tile.unit = null;
      unit.tile.renderer.updateRender();
      data.units.remove(unit);
      if (broadcast) {
        Civilisation.CLIENT.broadcast(new PacketUnitDelete(unit.tile.x, unit.tile.y));
      }
    }
  }

  public void updateTileRenders() {
    data.hexagonGrid.forEach((gridTile, _hex, _x, _y) -> gridTile.renderer.updateRender());
  }

  private RenderTile pathStartTile, pathEndTile;

  private RenderTile getTileFromPickResult(PickResult result) {
    if (result == null) return null;
    Node node = result.getIntersectedNode();
    if (node == null) return null;

    do {
      if (node instanceof RenderTile) return (RenderTile) node;
      node = node.getParent();
    } while (node != null);

    return null;
  }

  private void resetPathfindingEnd() {
    if (pathEndTile != null) {
      pathEndTile.setOverlayVisible(false);
      data.hexagonGrid.forEach((t, _hex, _x, _y) -> t.renderer.setOverlayVisible(false));
      pathEndTile = null;
    }
  }

  public void resetPathfinding() {
    if (pathStartTile != null && pathEndTile != null) {
      Path<Tile> path = data.hexagonGrid.findPath(
        pathStartTile.data.x,
        pathStartTile.data.y,
        pathEndTile.data.x,
        pathEndTile.data.y,
        pathStartTile.data.unit.remainingMovementPointsThisTurn
      );
      if (path.path.size() > 1) {
        int usedMovementPoints = path.totalCost;

        pathStartTile.data.unit.remainingMovementPointsThisTurn -= usedMovementPoints;
        assert pathStartTile.data.unit.remainingMovementPointsThisTurn >= 0;

        Civilisation.CLIENT.broadcast(new PacketUnitMove(
          pathStartTile.data.x,
          pathStartTile.data.y,
          pathEndTile.data.x,
          pathEndTile.data.y,
          usedMovementPoints
        ));
        pathStartTile.data.unit.tile = pathEndTile.data;
        pathEndTile.data.unit = pathStartTile.data.unit;
        pathStartTile.data.unit = null;

        pathEndTile.data.selected = pathStartTile.data.selected;
        pathStartTile.data.selected = false;

        pathStartTile.updateRender();
        pathEndTile.updateRender();

        selectedUnitListener.accept(data.selectedUnit);
      }
    }

    if (pathStartTile != null) {
      pathStartTile.setOverlayVisible(false);
      pathStartTile = null;
    }
    if (pathEndTile != null) {
      resetPathfindingEnd();
    }
  }

  public Tile[] handlePacket(Packet packet) {
    Tile[] tilesToUpdate = data.handlePacket(packet);
    if (tilesToUpdate != null) {
      if (tilesToUpdate.length == 0) {
        updateTileRenders();
      } else for (Tile tile : tilesToUpdate) {
        if (tile.unit != null && tile.unit.isDead()) {
          deleteUnit(tile.unit, false);
        } else {
          tile.renderer.updateRender();
        }
      }
    }
    return tilesToUpdate;
  }
}
