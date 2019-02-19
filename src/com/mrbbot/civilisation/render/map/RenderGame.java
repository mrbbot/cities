package com.mrbbot.civilisation.render.map;

import com.mrbbot.civilisation.Civilisation;
import com.mrbbot.civilisation.logic.Player;
import com.mrbbot.civilisation.logic.map.Game;
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
  public Player currentPlayer;

  public RenderGame(Game data, String id, Consumer<Unit> selectedUnitListener) {
    super(data);
    this.selectedUnitListener = selectedUnitListener;

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
      RenderTile renderTile = new RenderTile(tile);
      tile.renderer = renderTile;

      Point2D center = hex.getCenter();
      String coord = "(" + center.getX() + ", " + center.getY() + ")";
      renderTile.setOnMouseClicked((e) -> {
        if (data.waitingForPlayers) return;
        //System.out.println("You clicked on the tile at " + coord);

        if (e.getButton() == MouseButton.PRIMARY) {
          setSelectedUnit(tile.unit);
        } else if (e.getButton() == MouseButton.SECONDARY) {
          Unit target = tile.unit;
          Unit attacker = data.selectedUnit;

          // Check if both units exist
          if (target == null || attacker == null) return;

          // Check if target belongs to current player
          if(target.player.equals(currentPlayer)) return;

          // Check if the unit can even attack
          if(!attacker.hasAbility(UnitAbility.ABILITY_ATTACK) && !attacker.hasAbility(UnitAbility.ABILITY_RANGED_ATTACK)) return;

          // Check if attacked already this turn
          if(attacker.hasAttackedThisTurn) return;

          PacketUnitDamage packetUnitDamage = new PacketUnitDamage(attacker.tile.x, attacker.tile.y, target.tile.x, target.tile.y);
          handlePacket(packetUnitDamage);
          Civilisation.CLIENT.broadcast(packetUnitDamage);
        }
        /*if(tile.unit != null && tile.unit.player.equals(currentPlayer)) {
          if(data.selectedUnit != null) {
            data.selectedUnit.tile.selected = false;
            data.selectedUnit.tile.renderer.updateRender();
          }
          tile.selected = true;
          tile.renderer.updateRender();
          data.selectedUnit = tile.unit;
          selectedUnitListener.accept(data.selectedUnit);
        } else {
          if(data.selectedUnit != null) {
            data.selectedUnit.tile.selected = false;
            data.selectedUnit.tile.renderer.updateRender();
            data.selectedUnit = null;
            selectedUnitListener.accept(null);
          }
        }*/

        /*if (tile.city != null) {
          if (e.getButton() == MouseButton.PRIMARY) {
            ArrayList<SerializableIntPoint2D> grownTo = tile.city.grow(1);
            Civilisation.CLIENT.broadcast(new PacketCityGrow(currentPlayer.id, tile.x, tile.y, grownTo));
            updateTileRenders();
          } else if (e.getButton() == MouseButton.SECONDARY) {
            if (tile.improvement == Improvement.NONE) {
              tile.setImprovement(Improvement.FARM);
            } else if (tile.improvement == Improvement.FARM) {
              tile.setImprovement(Improvement.NONE);
            }
          }
        } else {
          Civilisation.CLIENT.broadcast(new PacketCityCreate(currentPlayer.id, tile.x, tile.y));
          data.cities.add(new City(data.hexagonGrid, tile.x, tile.y, currentPlayer));
          updateTileRenders();
        }*/
      });

      renderTile.setOnMouseDragged((e) -> {
        if (data.waitingForPlayers) return;

        if (e.getButton() == MouseButton.SECONDARY) {
          if (pathStartTile == null && renderTile.data.unit != null && renderTile.data.unit.player.equals(currentPlayer)) {
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
              );
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
      List<Tile> path = data.hexagonGrid.findPath(
        pathStartTile.data.x,
        pathStartTile.data.y,
        pathEndTile.data.x,
        pathEndTile.data.y,
        pathStartTile.data.unit.remainingMovementPointsThisTurn
      );
      if (path.size() > 1) {
        int usedMovementPoints = path.size() - 1;
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

  public void handlePacket(Packet packet) {
    Tile[] tilesToUpdate = data.handlePacket(packet);
    if (tilesToUpdate != null) {
      if (tilesToUpdate.length == 0) {
        updateTileRenders();
      } else for (Tile tile : tilesToUpdate) {
        if(tile.unit != null && tile.unit.health <= 0) {
          deleteUnit(tile.unit, false);
        } else {
          tile.renderer.updateRender();
        }
      }
    }
  }
}
